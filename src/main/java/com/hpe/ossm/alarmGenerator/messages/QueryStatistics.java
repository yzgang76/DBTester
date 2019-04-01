package com.hpe.ossm.alarmGenerator.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class QueryStatistics {
    private int id;
    private String sql;
    private long cost_a;
    private long cost_q;
    private long size;
    private int count;
    private LocalDateTime ts;
}
