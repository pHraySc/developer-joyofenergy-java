package uk.tw.energy.service;

import org.springframework.stereotype.Service;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PricePlanService {

	private final List<PricePlan> pricePlans;
	private final MeterReadingService meterReadingService;

	public PricePlanService(List<PricePlan> pricePlans, MeterReadingService meterReadingService) {
		this.pricePlans = pricePlans;
		this.meterReadingService = meterReadingService;
	}

	/**
	 * 获取指定智能电表的价格计划，与计算关联此电表读数对于每个价格计划的均价
	 *
	 * @param smartMeterId
	 * @return
	 */
	public Optional<Map<String, BigDecimal>> getConsumptionCostOfElectricityReadingsForEachPricePlan(String smartMeterId) {
		Optional<List<ElectricityReading>> electricityReadings = meterReadingService.getReadings(smartMeterId);

		if (!electricityReadings.isPresent()) {
			return Optional.empty();
		}

		// 返回价格计划名称与它关联此电表实时读数的成本均价 - 键值对
		return Optional.of(pricePlans.stream().collect(
				Collectors.toMap(PricePlan::getPlanName, t -> calculateCost(electricityReadings.get(), t))));
	}

	/**
	 * 计算指定(每个)价格计划(单价不同)对于指定电表读数的成本价格
	 *
	 * @param electricityReadings
	 * @param pricePlan
	 * @return
	 */
	private BigDecimal calculateCost(List<ElectricityReading> electricityReadings, PricePlan pricePlan) {
		// TODO
		BigDecimal average = calculateAverageReading(electricityReadings);
		BigDecimal timeElapsed = calculateTimeElapsed(electricityReadings);

		// TODO
		BigDecimal averagedCost = average.divide(timeElapsed, RoundingMode.HALF_UP);
		return averagedCost.multiply(pricePlan.getUnitRate());
	}

	/**
	 * 计划多次读数的平均值
	 *
	 * @param electricityReadings
	 * @return
	 */
	private BigDecimal calculateAverageReading(List<ElectricityReading> electricityReadings) {
		BigDecimal summedReadings = electricityReadings.stream()
				.map(ElectricityReading::getReading)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		return summedReadings.divide(BigDecimal.valueOf(electricityReadings.size()), RoundingMode.HALF_UP);
	}

	/**
	 * 计算多次读数的时间长度(单位: H)
	 *
	 * @param electricityReadings
	 * @return
	 */
	private BigDecimal calculateTimeElapsed(List<ElectricityReading> electricityReadings) {
		ElectricityReading first = electricityReadings.stream()
				.min(Comparator.comparing(ElectricityReading::getTime))
				.get();
		ElectricityReading last = electricityReadings.stream()
				.max(Comparator.comparing(ElectricityReading::getTime))
				.get();

		return BigDecimal.valueOf(Duration.between(first.getTime(), last.getTime()).getSeconds() / 3600.0);
	}

}
