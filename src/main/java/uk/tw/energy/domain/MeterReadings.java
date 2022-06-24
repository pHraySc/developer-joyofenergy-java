package uk.tw.energy.domain;

import java.util.List;

public class MeterReadings {

	private List<ElectricityReading> electricityReadings;
	private String smartMeterId;

	public MeterReadings() {}

	/**
	 * @param smartMeterId        智能电表Id
	 * @param electricityReadings 智能电表读数
	 */
	public MeterReadings(String smartMeterId, List<ElectricityReading> electricityReadings) {
		this.smartMeterId = smartMeterId;
		this.electricityReadings = electricityReadings;
	}

	public List<ElectricityReading> getElectricityReadings() {
		return electricityReadings;
	}

	public String getSmartMeterId() {
		return smartMeterId;
	}
}
