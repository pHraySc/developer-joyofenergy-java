package uk.tw.energy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.tw.energy.builders.MeterReadingsBuilder;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.MeterReadings;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = App.class)
public class EndpointTest {

	@Autowired
	private TestRestTemplate restTemplate;
	@Autowired
	private ObjectMapper mapper;

	@Test
	public void shouldStoreReadings() throws JsonProcessingException {
		MeterReadings meterReadings = new MeterReadingsBuilder().generateElectricityReadings().build();
		HttpEntity<String> entity = getStringHttpEntity(meterReadings);

		ResponseEntity<String> response = restTemplate.postForEntity("/readings/store", entity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void givenMeterIdShouldReturnAMeterReadingAssociatedWithMeterId() throws JsonProcessingException {
		String smartMeterId = "bob";
		populateMeterReadingsForMeter(smartMeterId);

		ResponseEntity<String> response = restTemplate.getForEntity("/readings/read/" + smartMeterId, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void shouldCalculateAllPrices() throws JsonProcessingException {
		String smartMeterId = "bob";
		populateMeterReadingsForMeter(smartMeterId);

		ResponseEntity<String> response = restTemplate.getForEntity("/price-plans/compare-all/" + smartMeterId,
				String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void givenMeterIdAndLimitShouldReturnRecommendedCheapestPricePlans() throws JsonProcessingException {
		String smartMeterId = "bob";
		populateMeterReadingsForMeter(smartMeterId);

		ResponseEntity<String> response =
				restTemplate.getForEntity("/price-plans/recommend/" + smartMeterId + "?limit=2", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void givenMeterIdAndGetMeterCost() throws JsonProcessingException {
		String smartMeterId = "smart-meter-0";
		MeterReadings meterReadings = populateMeterReadingsForMeterAndReturn(smartMeterId);

		ResponseEntity<BigDecimal> response =
				restTemplate.getForEntity("/cost-calculate/get-cost/" + smartMeterId, BigDecimal.class);
		assertThat(response.getBody()).isCloseTo(new BigDecimal(1800), withinPercentage(1));
	}

	private HttpEntity<String> getStringHttpEntity(Object object) throws JsonProcessingException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String jsonMeterData = mapper.writeValueAsString(object);
		return (HttpEntity<String>) new HttpEntity(jsonMeterData, headers);
	}

	private void populateMeterReadingsForMeter(String smartMeterId) throws JsonProcessingException {
		MeterReadings readings = new MeterReadingsBuilder().setSmartMeterId(smartMeterId)
				.generateElectricityReadings(20)
				.build();

		HttpEntity<String> entity = getStringHttpEntity(readings);
		restTemplate.postForEntity("/readings/store", entity, String.class);
	}

	private MeterReadings populateMeterReadingsForMeterAndReturn(String smartMeterId) throws JsonProcessingException {
		MeterReadings readings = new MeterReadings(smartMeterId, constructRecentReads());

		HttpEntity<String> entity = getStringHttpEntity(readings);
		restTemplate.postForEntity("/readings/store", entity, String.class);
		return readings;
	}

	private List<ElectricityReading> constructRecentReads() {
		List<ElectricityReading> reading = Lists.newArrayList();
		reading.add(new ElectricityReading(LocalDateTime.now().minusDays(5).toInstant(ZoneOffset.UTC),
				BigDecimal.TEN));
		reading.add(new ElectricityReading(LocalDateTime.now().minusDays(6).toInstant(ZoneOffset.UTC),
				new BigDecimal(5)));
		reading.add(new ElectricityReading(LocalDateTime.now().minusDays(8).toInstant(ZoneOffset.UTC),
				new BigDecimal(20)));
		return reading;
	}
}
