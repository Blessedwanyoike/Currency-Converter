package com.example.currencyconverter;

import javax.swing.SwingUtilities;

public class CurrencyConverterApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CurrencyConverterWindow(new ExchangeRateService()).show());
    }
}
