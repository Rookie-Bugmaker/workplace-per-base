package com.crrc.utils;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author yukun
 */
public final class TreeUtils {

    private TreeUtils() {}

    /** 孤儿（父不存在或 parentId 为 null）节点的处理策略 */
    public enum OrphanPolicy {
        /** 将孤儿作为根节点返回 */
        AS_ROOT,
        /** 直接丢弃孤儿节点 */
        DROP,
        /** 发现孤儿即抛出异常 */
        ERROR
    }

    /**
     * 构建树并写回 children，返回根节点列表。
     * - 孤儿作为根返回
     * - 同级不排序
     * - children 列表使用 ArrayList
     * @param idGetter 获得自己ID
     * @param parentIdGetter 获得父亲ID
     * @param childrenSetter children的setter
     */
    public static <T, K> List<T> buildCascadeList(
            Collection<T> items,
            Function<T, K> idGetter,
            Function<T, K> parentIdGetter,
            BiConsumer<T, List<T>> childrenSetter
    ) {
        return buildCascadeList(
                items, idGetter, parentIdGetter, childrenSetter,
                ArrayList::new, null, OrphanPolicy.AS_ROOT
        );
    }

    /**
     * 可配置 childrenListFactory、同级排序 Comparator、孤儿策略
     * @param items              原始扁平列表
     * @param idGetter           从实体提取唯一 ID
     * @param parentIdGetter     从实体提取 parentId
     * @param childrenSetter     将计算后的子列表写回实体
     * @param childrenListFactory 子列表的构造器（如 ArrayList::new）
     * @param order              同级排序（可为 null 表示不排序）
     * @param orphanPolicy       孤儿节点处理策略
     * @return 根节点列表（已填充 children）
     */
    public static <T, K> List<T> buildCascadeList(
            Collection<T> items,
            Function<T, K> idGetter,
            Function<T, K> parentIdGetter,
            BiConsumer<T, List<T>> childrenSetter,
            Supplier<List<T>> childrenListFactory,
            Comparator<T> order,
            OrphanPolicy orphanPolicy
    ) {
        Objects.requireNonNull(idGetter, "idGetter");
        Objects.requireNonNull(parentIdGetter, "parentIdGetter");
        Objects.requireNonNull(childrenSetter, "childrenSetter");
        Objects.requireNonNull(childrenListFactory, "childrenListFactory");
        Objects.requireNonNull(orphanPolicy, "orphanPolicy");

        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        // 建立id -> 节点的索引，用LinkedHashMap保持稳定
        Map<K, T> byId = new LinkedHashMap<>(Math.max(16, items.size() * 2));
        for (T t : items) {
            K id = idGetter.apply(t);
            if (id == null) {
                throw new IllegalArgumentException("id 不能为 null");
            }
            // 避免后写覆盖前写
            if (byId.putIfAbsent(id, t) != null) {
                throw new IllegalArgumentException("检测到重复 id: " + id);
            }
        }

        // 预分配每个节点的children容器
        Map<K, List<T>> childrenBuckets = new HashMap<>(byId.size() * 2);
        for (K id : byId.keySet()) {
            childrenBuckets.put(id, childrenListFactory.get());
        }

        // 分发到父节点的children桶，并收集根
        List<T> roots = new ArrayList<>();
        for (T t : items) {
            K id = idGetter.apply(t);
            K pid = parentIdGetter.apply(t);

            // 当作无效父引用 -> 走孤儿分支
            boolean selfLoop = Objects.equals(id, pid);

            if (pid == null || selfLoop || !byId.containsKey(pid)) {
                switch (orphanPolicy) {
                    case AS_ROOT:
                        roots.add(t);
                        break;
                    case DROP:
                        // 丢弃：不加入任何父，也不作为根返回
                        break;
                    case ERROR:
                        throw new IllegalArgumentException(
                                "发现孤儿节点或自指（父不存在/parentId=null/self-loop）。id=" + id + ", parentId=" + pid
                        );
                    default:
                }
            } else {
                childrenBuckets.get(pid).add(t);
            }
        }

        // 同级排序（如配置），并写回 children
        if (order != null) {
            for (List<T> siblings : childrenBuckets.values()) {
                siblings.sort(order);
            }
            roots.sort(order);
        }

        for (Map.Entry<K, List<T>> e : childrenBuckets.entrySet()) {
            T parent = byId.get(e.getKey());
            if (parent != null) {
                // 强制用新的列表覆盖
                childrenSetter.accept(parent, e.getValue());
            }
        }

        return roots;
    }

    /**
     * 级联展平树节点，前序DFS
     * @param root 需要展平的根
     * @param childrenGetter 获取树节点中儿子列表的方法
     * @param containRoot 是否包含root
     * @return 展平后的树节点，根据containRoot判断是否包含root
     */
    public static <T> List<T> flattenDescendants(T root, Function<T, List<T>> childrenGetter, Boolean containRoot) {
        List<T> out = new ArrayList<>(containRoot ? Collections.singletonList(root) : Collections.emptyList());
        if(childrenGetter.apply(root) == null || childrenGetter.apply(root).isEmpty()) {
            return out;
        }

        Deque<T> stack = new ArrayDeque<>();
        // 先把第一层儿子入栈（倒序以保持原 children 顺序）
        List<T> first = childrenGetter.apply(root);
        for (int i = first.size() - 1; i >= 0; i--) {
            stack.push(first.get(i));
        }

        while (!stack.isEmpty()) {
            T cur = stack.pop();
            out.add(cur);
            if (childrenGetter.apply(cur) != null) {
                List<T> apply = childrenGetter.apply(cur);
                // 同理：倒序压栈，保持遍历顺序与 children 列表一致
                for (int i = apply.size() - 1; i >= 0; i--) {
                    stack.push(apply.get(i));
                }
            }
        }

        return out;
    }

    /**
     * 查询树是否匹配某个Predicate
     * @param root 查询的树根
     * @param matcher Predicate条件
     * @param childrenGetter children的Get方法
     * @return 是否包含Predicate
     */
    public static <T> Boolean containsBfs(T root, Predicate<T> matcher, Function<T, List<T>> childrenGetter) {
        return findNodeBfs(root, matcher, childrenGetter).isPresent();
    }

    /**
     * 查询树的节点
     * @param root 查询的树根
     * @param matcher Predicate条件
     * @param childrenGetter children的Get方法
     * @return 是否包含Predicate
     */
    public static <T> Optional<T> findNodeBfs(T root, Predicate<T> matcher, Function<T, List<T>> childrenGetter) {
        if(childrenGetter.apply(root) == null || matcher == null) {
            return Optional.empty();
        }
        Queue<T> q = new ArrayDeque<>();
        q.offer(root);
        while(!q.isEmpty()) {
            T node = q.poll();
            if(matcher.test(node)) {
                return Optional.of(node);
            }
            for(T c : childrenGetter.apply(node)) {
                q.offer(c);
            }
        }
        return Optional.empty();
    }
}