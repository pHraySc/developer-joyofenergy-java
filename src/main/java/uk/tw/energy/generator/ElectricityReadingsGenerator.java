package uk.tw.energy.generator;

import uk.tw.energy.domain.ElectricityReading;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class ElectricityReadingsGenerator {

	/**
	 * <p>生成读数：</p>
	 * 1-随机4位小数的读数； <br>
	 * 2-时间=now倒推n*10s，每10s一个读数； <br>
	 * 按时间升序
	 *
	 * @param number 生成多少个读数
	 * @return
	 */
	public List<ElectricityReading> generate(int number) {
		List<ElectricityReading> readings = new ArrayList<>();
		Instant now = Instant.now();

		Random readingRandomiser = new Random();
		for (int i = 0; i < number; i++) {
			double positiveRandomValue = Math.abs(readingRandomiser.nextGaussian());
			BigDecimal randomReading = BigDecimal.valueOf(positiveRandomValue).setScale(4, RoundingMode.CEILING);
			ElectricityReading electricityReading = new ElectricityReading(now.minusSeconds(i * 10), randomReading);
			readings.add(electricityReading);
		}

		readings.sort(Comparator.comparing(ElectricityReading::getTime));
		return readings;
	}
}
