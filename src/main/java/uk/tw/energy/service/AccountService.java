package uk.tw.energy.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AccountService {

	private final Map<String, String> smartMeterToPricePlanAccounts;

	/**
	 * @param smartMeterToPricePlanAccounts <智能电表Id, 价格计划Id>
	 */
	public AccountService(Map<String, String> smartMeterToPricePlanAccounts) {
		this.smartMeterToPricePlanAccounts = smartMeterToPricePlanAccounts;
	}

	/**
	 * 获取智能电表对应的价格计划Id
	 *
	 * @param smartMeterId
	 * @return
	 */
	public String getPricePlanIdForSmartMeterId(String smartMeterId) {
		return smartMeterToPricePlanAccounts.get(smartMeterId);
	}
}
