package uk.tw.energy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.PricePlanService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/price-plans")
public class PricePlanComparatorController {

	public final static String PRICE_PLAN_ID_KEY = "pricePlanId";
	public final static String PRICE_PLAN_COMPARISONS_KEY = "pricePlanComparisons";
	private final PricePlanService pricePlanService;
	private final AccountService accountService;

	public PricePlanComparatorController(PricePlanService pricePlanService, AccountService accountService) {
		this.pricePlanService = pricePlanService;
		this.accountService = accountService;
	}

	/**
	 * （当前价格计划与所有价格计划的成本比较）
	 * 获取指定智能电表的价格计划，与计算关联此电表读数对于每个价格计划的均价
	 *
	 * @param smartMeterId
	 * @return
	 */
	@GetMapping("/compare-all/{smartMeterId}")
	public ResponseEntity<Map<String, Object>> calculatedCostForEachPricePlan(@PathVariable String smartMeterId) {
		String pricePlanId = accountService.getPricePlanIdForSmartMeterId(smartMeterId);
		Optional<Map<String, BigDecimal>> consumptionsForPricePlans =
				pricePlanService.getConsumptionCostOfElectricityReadingsForEachPricePlan(smartMeterId);

		if (!consumptionsForPricePlans.isPresent()) {
			return ResponseEntity.notFound().build();
		}

		Map<String, Object> pricePlanComparisons = new HashMap<>();
		pricePlanComparisons.put(PRICE_PLAN_ID_KEY, pricePlanId);
		pricePlanComparisons.put(PRICE_PLAN_COMPARISONS_KEY, consumptionsForPricePlans.get());

		return consumptionsForPricePlans.isPresent()
				? ResponseEntity.ok(pricePlanComparisons)
				: ResponseEntity.notFound().build();
	}

	/**
	 * 获取指定智能电表对于每个价格计划的实时均价，并按均价升序排序
	 *
	 * @param smartMeterId
	 * @param limit
	 * @return
	 */
	@GetMapping("/recommend/{smartMeterId}")
	public ResponseEntity<List<Map.Entry<String, BigDecimal>>> recommendCheapestPricePlans(
			@PathVariable String smartMeterId, @RequestParam(value = "limit", required = false) Integer limit) {
		// 1-获取指定智能电表的价格计划，与计算关联此电表读数对于每个价格计划的均价
		Optional<Map<String, BigDecimal>> consumptionsForPricePlans =
				pricePlanService.getConsumptionCostOfElectricityReadingsForEachPricePlan(smartMeterId);

		if (!consumptionsForPricePlans.isPresent()) {
			return ResponseEntity.notFound().build();
		}

		// 2-按计算出的均价排序asc
		List<Map.Entry<String, BigDecimal>> recommendations =
				new ArrayList<>(consumptionsForPricePlans.get().entrySet());
		recommendations.sort(Comparator.comparing(Map.Entry::getValue));

		// 3-有限制则截取
		if (limit != null && limit < recommendations.size()) {
			recommendations = recommendations.subList(0, limit);
		}

		return ResponseEntity.ok(recommendations);
	}
}
