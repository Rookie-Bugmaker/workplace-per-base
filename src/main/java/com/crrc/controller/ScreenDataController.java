package com.crrc.controller;

import com.crrc.annotation.TenantIgnore;
import com.crrc.entity.HrEmpBasic;
import com.crrc.pojo.ResponseResult;
import com.crrc.service.impl.HrEmpBasicServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/external/screenData")
public class ScreenDataController {

    @Autowired
    HrEmpBasicServiceImpl hrEmpBasicService;

    private static final Map<String, String[]> AREA_COORDS = new LinkedHashMap<>();

    static {
        AREA_COORDS.put("北京", new String[]{"116.4074", "39.9042"});
        AREA_COORDS.put("天津", new String[]{"117.2010", "39.0842"});
        AREA_COORDS.put("河北", new String[]{"114.5149", "38.0428"});
        AREA_COORDS.put("山西", new String[]{"112.5492", "37.8706"});
        AREA_COORDS.put("内蒙古", new String[]{"111.7492", "40.8426"});
        AREA_COORDS.put("辽宁", new String[]{"123.4315", "41.8057"});
        AREA_COORDS.put("吉林", new String[]{"125.3235", "43.8171"});
        AREA_COORDS.put("黑龙江", new String[]{"126.6424", "45.7560"});
        AREA_COORDS.put("上海", new String[]{"121.4737", "31.2304"});
        AREA_COORDS.put("江苏", new String[]{"118.7969", "32.0603"});
        AREA_COORDS.put("浙江", new String[]{"120.1551", "30.2741"});
        AREA_COORDS.put("安徽", new String[]{"117.2272", "31.8206"});
        AREA_COORDS.put("福建", new String[]{"119.2965", "26.0745"});
        AREA_COORDS.put("江西", new String[]{"115.8582", "28.6829"});
        AREA_COORDS.put("山东", new String[]{"117.1201", "36.6512"});
        AREA_COORDS.put("河南", new String[]{"113.6254", "34.7466"});
        AREA_COORDS.put("湖北", new String[]{"114.3054", "30.5931"});
        AREA_COORDS.put("湖南", new String[]{"112.9388", "28.2282"});
        AREA_COORDS.put("广东", new String[]{"113.2644", "23.1291"});
        AREA_COORDS.put("广西", new String[]{"108.3669", "22.8170"});
        AREA_COORDS.put("海南", new String[]{"110.1983", "20.0440"});
        AREA_COORDS.put("重庆", new String[]{"106.5516", "29.5630"});
        AREA_COORDS.put("四川", new String[]{"104.0665", "30.5723"});
        AREA_COORDS.put("贵州", new String[]{"106.6302", "26.6470"});
        AREA_COORDS.put("云南", new String[]{"102.8329", "24.8801"});
        AREA_COORDS.put("西藏", new String[]{"91.1409", "29.6456"});
        AREA_COORDS.put("陕西", new String[]{"108.9398", "34.3416"});
        AREA_COORDS.put("甘肃", new String[]{"103.8343", "36.0611"});
        AREA_COORDS.put("青海", new String[]{"101.7782", "36.6171"});
        AREA_COORDS.put("宁夏", new String[]{"106.2309", "38.4872"});
        AREA_COORDS.put("新疆", new String[]{"87.6168", "43.8256"});
        AREA_COORDS.put("香港", new String[]{"114.1694", "22.3193"});
        AREA_COORDS.put("澳门", new String[]{"113.5439", "22.1987"});
        AREA_COORDS.put("台湾", new String[]{"121.5654", "25.0330"});

        // 海外没有统一坐标，这里使用占位坐标
        AREA_COORDS.put("海外", new String[]{"91", "10"});
    }

    @TenantIgnore
    @GetMapping("/gPrsnSrcAddr")
    public ResponseResult<List<Map<String, String>>> getPersonSourceAddress() {
        Map<String, Long> countList = hrEmpBasicService.lambdaQuery()
                .select(HrEmpBasic::getNativePlaceName)
                .list()
                .stream()
                .map(emp ->
                    getRegion(emp.getNativePlaceName())
                ).collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));

        List<Map<String, String>> data = countList.entrySet().stream()
                .map(
                        e -> {
                            HashMap<String, String> map = new HashMap<>();
                            if (AREA_COORDS.containsKey(e.getKey())) {
                                map.put("name", e.getKey());
                                map.put("lng", AREA_COORDS.get(e.getKey())[0]);
                                map.put("lat", AREA_COORDS.get(e.getKey())[1]);
                                map.put("count", String.valueOf(e.getValue()));
                            }

                            return map;
                        }
                ).collect(Collectors.toList());

        return ResponseResult.success(data);
    }

    private static String getRegion(String place) {
        if (place == null || place.trim().isEmpty()) {
            return null;
        }

        String value = place.trim();

        return AREA_COORDS.keySet().stream()
                .filter(value::contains)
                .min(Comparator.comparingInt(value::indexOf))
                .orElse("海外");
    }
}
