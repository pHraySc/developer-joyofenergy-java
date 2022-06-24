package uk.tw.energy.domain;

import java.math.BigDecimal;
import java.time.Instant;

public class ElectricityReading {

	private Instant time;
	private BigDecimal reading; // kW

	public ElectricityReading() {}

	/**
	 * @param time    读数时间
	 * @param reading 读数值
	 */
	public ElectricityReading(Instant time, BigDecimal reading) {
		this.time = time;
		this.reading = reading;
	}

	public BigDecimal getReading() {
		return reading;
	}

	public Instant getTime() {
		return time;
	}
}
