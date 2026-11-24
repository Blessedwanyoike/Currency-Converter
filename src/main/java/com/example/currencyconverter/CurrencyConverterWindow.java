package com.example.currencyconverter;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

public class CurrencyConverterWindow {
    private static final String[] CURRENCIES = {
            "USD", "EUR", "GBP", "CAD", "AUD", "JPY", "CHF", "CNY", "INR"
    };

    private final ExchangeRateService exchangeRateService;
    private final JFrame frame = new JFrame("Currency Converter");
    private final JTextField amountField = new JTextField("100", 12);
    private final JComboBox<String> fromCurrency = new JComboBox<>(CURRENCIES);
    private final JComboBox<String> toCurrency = new JComboBox<>(CURRENCIES);
    private final JButton convertButton = new JButton("Convert");
    private final JLabel resultLabel = new JLabel("Enter an amount and select currencies.");

    public CurrencyConverterWindow(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
        fromCurrency.setEditable(true);
        toCurrency.setEditable(true);
        fromCurrency.setSelectedItem("USD");
        toCurrency.setSelectedItem("EUR");
        convertButton.addActionListener(event -> convert());

        JPanel fields = new JPanel(new GridLayout(3, 2, 8, 8));
        fields.add(new JLabel("Amount"));
        fields.add(amountField);
        fields.add(new JLabel("From"));
        fields.add(fromCurrency);
        fields.add(new JLabel("To"));
        fields.add(toCurrency);

        JPanel content = new JPanel(new BorderLayout(8, 12));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        content.add(fields, BorderLayout.NORTH);
        content.add(convertButton, BorderLayout.CENTER);
        content.add(resultLabel, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(content);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setLocationRelativeTo(null);
    }

    public void show() {
        frame.setVisible(true);
    }

    private void convert() {
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText().trim());
            if (amount.signum() < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            showError("Enter a valid non-negative amount.");
            return;
        }

        String from = String.valueOf(fromCurrency.getSelectedItem());
        String to = String.valueOf(toCurrency.getSelectedItem());
        convertButton.setEnabled(false);
        resultLabel.setText("Converting...");

        new SwingWorker<ExchangeRateService.ConversionResult, Void>() {
            @Override
            protected ExchangeRateService.ConversionResult doInBackground() throws Exception {
                return exchangeRateService.convert(from, to, amount);
            }

            @Override
            protected void done() {
                convertButton.setEnabled(true);
                try {
                    ExchangeRateService.ConversionResult result = get();
                    resultLabel.setText(result.amount().toPlainString() + " " + result.from()
                            + " = " + result.convertedAmount().toPlainString() + " " + result.to()
                            + " (rate date: " + result.rateDate() + ")");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("Conversion was interrupted.");
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    showError(cause.getMessage() == null ? "Currency conversion failed." : cause.getMessage());
                }
            }
        }.execute();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Conversion error", JOptionPane.ERROR_MESSAGE);
    }
}
