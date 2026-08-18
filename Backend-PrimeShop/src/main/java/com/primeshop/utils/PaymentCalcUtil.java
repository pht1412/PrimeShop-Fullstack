package com.primeshop.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PaymentCalcUtil {
    public static BigDecimal calculateMonthly(BigDecimal principal, BigDecimal annualRatePercent, int months) {
      if (annualRatePercent.compareTo(BigDecimal.ZERO) == 0) {
        return principal.divide(BigDecimal.valueOf(months), RoundingMode.HALF_UP);
      }
      BigDecimal monthlyRate = annualRatePercent.divide(BigDecimal.valueOf(12*100), 18, RoundingMode.HALF_UP);
      BigDecimal onePlusRPowerN = (BigDecimal.ONE.add(monthlyRate)).pow(months);
      BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowerN);
      BigDecimal denom = onePlusRPowerN.subtract(BigDecimal.ONE);
      return numerator.divide(denom, 0, RoundingMode.HALF_UP); // round to smallest unit
    }
  }
