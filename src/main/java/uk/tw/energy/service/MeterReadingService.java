package uk.tw.energy.service;

import org.springframework.stereotype.Service;
import uk.tw.energy.domain.ElectricityReading;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

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

	public List<ElectricityReading> getReadingsForSpecifiedDuration(String smartMeterId,
																	LocalDateTime duration) {
		if (Objects.isNull(duration)) {
			throw new RuntimeException("指定时间范围入参不能为空");
		}

		Optional<List<ElectricityReading>> allReadings = this.getReadings(smartMeterId);
		return allReadings
				.map(electricityReadings -> electricityReadings
						.stream()
						.filter(reading -> reading.getTime().isAfter(duration.toInstant(ZoneOffset.UTC)))
						.collect(Collectors.toList()))
				.orElseGet(Collections::emptyList);

	}
}
