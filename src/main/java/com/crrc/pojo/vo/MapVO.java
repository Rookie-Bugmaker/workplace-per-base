package com.crrc.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MapVO<K, V> {
    private K key;
    private V value;
}
