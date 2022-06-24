/**
 * pHray_sc
 * Copyright (c) 1970-2022 All Rights Reserved
 */
package uk.tw.energy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.MeterReadingService;
import uk.tw.energy.service.PricePlanService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 电表成本计算&获取
 *
 * @author Phray
 * @Version MeterCostController.java, v 0.1 2022-06-24 14:47 Sccc Exp $
 */
@RestController
@RequestMapping(value = "/cost-calculate")
public class MeterCostController {

	private final static long SPECIFIED_DURATION = 7;

	private final AccountService accountService;

	private final PricePlanService pricePlanService;

	private final MeterReadingService meterReadingService;

	public MeterCostController(
			AccountService accountService,
			PricePlanService pricePlanService,
			MeterReadingService meterReadingService) {
		this.accountService = accountService;
		this.pricePlanService = pricePlanService;
		this.meterReadingService = meterReadingService;
	}

	/**
	 * 根据指定智能电表关联的价格计划计算一周时间的电费使用成本
	 *
	 * @param smartMeterId
	 */
	@GetMapping(value = "/get-cost/{smartMeterId}")
	public ResponseEntity<BigDecimal> calculateMeterCost(@PathVariable String smartMeterId) {
		// 1-查询智能电表关联的价格计划，如果没有价格计划，则返回错误信息
		String pricePlanId = accountService.getPricePlanIdForSmartMeterId(smartMeterId);
		if (!haveRelatePricePlan(pricePlanId)) {
			return ResponseEntity.ok(new BigDecimal(-1));
		}
		// 2-如果存在关联的价格计划，则查询电表一周内读数
		List<ElectricityReading> readings = meterReadingService.getReadingsForSpecifiedDuration(smartMeterId,
				LocalDateTime.now().minusDays(SPECIFIED_DURATION));

		if (CollectionUtils.isEmpty(readings)) {
			return ResponseEntity.ok(BigDecimal.ZERO);
		}

		// 3-计算成本
		return ResponseEntity.ok(pricePlanService.calculateMultiplyCost(readings, pricePlanService.getById(pricePlanId)));
	}

	private boolean haveRelatePricePlan(String pricePlanId) {
		return pricePlanId != null && !pricePlanId.isEmpty();
	}
}
