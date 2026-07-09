package com.project.tas_pbo.service;

import com.project.tas_pbo.model.Member;

public class DiscountService {

    public double getDiscountRate(Member member, double totalBelanja) {

        if (member == null) {
            return 0;
        }

        if (totalBelanja >= 500_000) {
            return 0.30;
        } else if (totalBelanja >= 300_000) {
            return 0.20;
        } else if (totalBelanja >= 100_000) {
            return 0.10;
        }

        return 0;
    }

    public double getDiscountAmount(Member member, double totalBelanja) {
        return totalBelanja * getDiscountRate(member, totalBelanja);
    }

    public double getFinalTotal(Member member, double totalBelanja) {
        return totalBelanja - getDiscountAmount(member, totalBelanja);
    }

}
