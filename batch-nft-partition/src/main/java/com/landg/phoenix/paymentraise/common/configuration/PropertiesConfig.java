package com.landg.phoenix.paymentraise.common.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 
 * Property config class
 * 
 */

@Component
@Configuration
public class PropertiesConfig {

	@Value("${phoenix.landg.timetraveldate}")
	private String timeTravelDate;

	@Value("${phoenix.landg.leadtime}")
	private int leadTime;

	@Value("${phoenix.queue.master2worker}")
	private String masterToWorkerQueueName;

	@Value("${phoenix.queue.temp}")
	private String tempQueueName;

	@Value("${phoenix.partition.size}")
	private int grid;

	public String getTimeTravelDate() {
		return this.timeTravelDate;
	}

	public void setTimeTravelDate(String timeTravelDate) {
		this.timeTravelDate = timeTravelDate;
	}

	public int getLeadTime() {
		return this.leadTime;
	}

	public void setLeadTime(int leadTime) {
		this.leadTime = leadTime;
	}

	public String getMasterToWorkerQueueName() {
		return masterToWorkerQueueName;
	}

	public void setMasterToWorkerQueueName(String masterToWorkerQueueName) {
		this.masterToWorkerQueueName = masterToWorkerQueueName;
	}

	public String getTempQueueName() {
		return tempQueueName;
	}

	public void setTempQueueName(String tempQueueName) {
		this.tempQueueName = tempQueueName;
	}

	public int getGrid() {
		return grid;
	}

	public void setGrid(int grid) {
		this.grid = grid;
	}

	@Override
	public String toString() {
		return "PropertiesConfig{" +
				"timeTravelDate='" + timeTravelDate + '\'' +
				", leadTime=" + leadTime +
				", masterToWorkerQueueName='" + masterToWorkerQueueName + '\'' +
				", tempQueueName='" + tempQueueName + '\'' +
				", grid='" + grid + '\'' +
				'}';
	}
}
