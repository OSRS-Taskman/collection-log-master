package com.collectionlogmaster.taskapp.response;

import com.collectionlogmaster.domain.TieredTaskList;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaskListResponse extends TieredTaskList {

}
