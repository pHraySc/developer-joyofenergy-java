package uk.tw.energy.service;

import org.springframework.stereotype.Service;
import uk.tw.energy.domain.ElectricityReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MeterReadingService {

	private final Map<String, List<ElectricityReading>> meterAssociatedReadings;

	public MeterReadingService(Map<String, List<ElectricityReading>> meterAssociatedReadings) {
		this.meterAssociatedReadings = meterAssociatedReadings;
	}

	/**
	 * 获取指定智能电表的读数(读数包含时间和实时功耗)
	 *
	 * @param smartMeterId
	 * @return
	 */
	public Optional<List<ElectricityReading>> getReadings(String smartMeterId) {
		return Optional.ofNullable(meterAssociatedReadings.get(smartMeterId));
	}

	/**
	 * 存储电表读数到Map，key: 电表Id, Value: 读数列表
	 *
	 * @param smartMeterId
	 * @param electricityReadings
	 */
	public void storeReadings(String smartMeterId, List<ElectricityReading> electricityReadings) {
		if (!meterAssociatedReadings.containsKey(smartMeterId)) {
			meterAssociatedReadings.put(smartMeterId, new ArrayList<>());
		}
		meterAssociatedReadings.get(smartMeterId).addAll(electricityReadings);
	}
}
