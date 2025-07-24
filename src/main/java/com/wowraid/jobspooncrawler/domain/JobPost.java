
package com.wowraid.jobspooncrawler.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JobPost {
    private String title;
    private String company;
    private List<String> techStacks;
    private String duties;
    private String requirements;
    private String preferred;
    private String benefits;
    private String deadline;
    private String location;
}
