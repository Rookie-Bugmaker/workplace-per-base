package com.crrc.utils;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

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
            validateAndIndexNode(idGetter, t, byId);
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
                processOrphanNode(orphanPolicy, t, roots, id, pid);
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
     * 验证节点有效性并建立ID索引
     * @param t 需要建立的节点
     * @param byId 索引map
     * @throws IllegalArgumentException 如果id为null或存在重复id
     */
    private static <T, K> void validateAndIndexNode(Function<T, K> idGetter, T t, Map<K, T> byId) {
        K id = idGetter.apply(t);
        if (id == null) {
            throw new IllegalArgumentException("id 不能为 null");
        }
        // 避免后写覆盖前写
        if (byId.putIfAbsent(id, t) != null) {
            throw new IllegalArgumentException("检测到重复 id: " + id);
        }
    }

    /**
     * 处理孤儿节点（无父节点或父节点无效）
     * @param t 孤儿节点
     * @param roots 根节点列表
     * @param id 用于打印的数据，当前节点的id
     * @param pid 用于打印的数据，当前节点的pid
     * @param orphanPolicy 处理策略（AS_ROOT/DROP/ERROR）
     * @throws IllegalArgumentException 如果策略为ERROR且发现孤儿节点
     */
    private static <T, K> void processOrphanNode(OrphanPolicy orphanPolicy, T t, List<T> roots, K id, K pid) {
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
    }

    /**
     * 获得目标节点的所有祖先节点，顺序：root -> ... -> parent
     * @param root 查询的树根
     * @param matcher Predicate条件(判定节点是否为目标节点的方法)，例如: node.getId().equals(123L)
     * @param childrenGetter 获取树节点中儿子列表的方法
     * @return 所有包含的parents列表
     */
    public static <T> Optional<List<T>> findParents(T root, Predicate<T> matcher, Function<T, List<T>> childrenGetter) {
        if(root == null || matcher == null) {
            return Optional.empty();
        }

        Deque<T> path = new ArrayDeque<>(); // 保存当前 DFS 路径上的祖先（不含目标节点）
        List<T> result = new ArrayList<>();

        boolean found = dfsFindAncestors(root, matcher, childrenGetter, path, result);
        return found ? Optional.of(result) : Optional.empty();
    }

    // DFS：path 里始终保存 “当前节点的祖先链（不含当前节点）”，但在命中目标时不把目标加入 path
    private static <T> boolean dfsFindAncestors(T current, Predicate<T> matcher, Function<T, List<T>> childrenGetter, Deque<T> path, List<T> result) {
        if (current == null) return false;

        // 当前节点作为后续子树目标的祖先，先入栈
        path.addLast(current);

        // 命中目标：此时 path 就是所有祖先（不含目标）
        if (matcher.test(current)) {
            result.addAll(path);
            return true;
        }

        for (T child : childrenGetter.apply(current)) {
            if (dfsFindAncestors(child, matcher, childrenGetter, path, result)) {
                return true; // 已找到目标，直接结束
            }
        }

        // 回溯
        path.removeLast();
        return false;
    }

    /**
     * 级联展平树节点，前序DFS
     * @param root 需要展平的根
     * @param childrenGetter 获取树节点中儿子列表的方法
     * @param containRoot 是否包含root
     * @return 展平后的树节点，根据containRoot判断是否包含root
     */
    public static <T> List<T> flattenDescendants(T root, Function<T, List<T>> childrenGetter, Boolean containRoot) {
        List<T> out = new ArrayList<>(Boolean.TRUE.equals(containRoot) ? Collections.singletonList(root) : Collections.emptyList());
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

    /**
     * 深度遍历树，执行某个方法
     * @param root 查询的树根
     * @param f 需要执行的函数
     * @param childrenGetter children的Get方法
     */
    public static <T> void doFunctionBFS(T root, Consumer<T> f, Function<T, List<T>> childrenGetter) {
        if(childrenGetter.apply(root) == null || f == null) {
            return;
        }

        Queue<T> q = new ArrayDeque<>();
        q.offer(root);
        while(!q.isEmpty()) {
            T node = q.poll();
            f.accept(node);
            for(T c : childrenGetter.apply(node)) {
                q.offer(c);
            }
        }
    }

    /**
     * 过滤树节点，当父节点不符合需求时，其所有子节点会被直接过滤掉！
     * @param nodes 当前处理的节点（列表）
     * @param predicate 过滤条件 (返回 true 保留，false 丢弃)
     * @param childrenGetter children的Get方法
     * @param childrenSetter children的Set方法
     * @return 过滤后的节点，如果不符合条件则返回空列表
     */
    public static <T> List<T> filterTree(List<T> nodes, Predicate<T> predicate, Function<T, List<T>> childrenGetter, BiConsumer<T, List<T>> childrenSetter) {
        nodes.forEach(node ->
                filterTree(node, predicate, childrenGetter, childrenSetter)
        );

        return nodes.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 过滤树节点，当父节点不符合需求时，其所有子节点会被直接过滤掉
     * @param node 当前处理的节点
     * @param predicate 过滤条件 (返回 true 保留，false 丢弃)
     * @param childrenGetter children的Get方法
     * @param childrenSetter children的Set方法
     * @return 过滤后的节点，如果不符合条件则返回 null
     */
    public static <T> T filterTree(T node, Predicate<T> predicate, Function<T, List<T>> childrenGetter, BiConsumer<T, List<T>> childrenSetter) {
        // 1. 【核心剪枝逻辑】
        // 如果当前节点本身就不符合条件，直接返回 null。
        // 根据你的描述，不需要再看它的 children 了。
        if (!predicate.test(node)) {
            return null;
        }

        // 2. 如果当前节点符合，继续递归处理子节点
        List<T> originalChildren = childrenGetter.apply(node);
        if (originalChildren != null && !originalChildren.isEmpty()) {
            List<T> validChildren = new ArrayList<>();

            for (T child : originalChildren) {
                // 递归调用
                T filteredChild = filterTree(child, predicate, childrenGetter, childrenSetter);
                // 如果子节点返回不为 null，说明子节点（及其后代）符合要求
                if (filteredChild != null) {
                    validChildren.add(filteredChild);
                }
            }

            // 3. 更新当前节点的子节点列表
            childrenSetter.accept(node, validChildren);
        }

        // 4. 返回当前节点（此时它已经包含了经过过滤的子节点列表）
        return node;
    }
}