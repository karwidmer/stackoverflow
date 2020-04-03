package com.landg.phoenix.paymentraise.worker.batch.entities;

import java.io.Serializable;
import java.sql.Timestamp;

import lombok.Data;

/**
 * 
 * Entity to hold Diary event
 * 
 */

@Data
public class DiaryEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long diaryUid;

	private String policy;

	private String diaryType;

	private Timestamp diaryTimestamp;

	private Timestamp createdTimestamp;

	private String diarySource;

	private String diaryData;
	
	private String status;
}
