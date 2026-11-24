package com.example.currencyconverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ExchangeRateService {
    private static final Pattern CURRENCY_CODE = Pattern.compile("[A-Za-z]{3}");
    private static final String API_BASE_URL = "https://api.frankfurter.dev/v2";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ConversionResult convert(String from, String to, BigDecimal amount) throws IOException, InterruptedException {
        String base = normalizeCurrency(from);
        String quote = normalizeCurrency(to);
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Amount must be non-negative.");
        }
        if (base.equals(quote)) {
            return new ConversionResult(base, quote, amount, BigDecimal.ONE,
                    amount.setScale(2, RoundingMode.HALF_UP), LocalDate.now(ZoneOffset.UTC));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/rate/" + base + "/" + quote))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 400 || response.statusCode() == 404) {
            throw new IllegalArgumentException("Unsupported currency code.");
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Exchange-rate lookup failed (HTTP " + response.statusCode() + ").");
        }

        RateResponse rateResponse = objectMapper.readValue(response.body(), RateResponse.class);
        if (rateResponse == null || rateResponse.rate() == null || rateResponse.date() == null) {
            throw new IOException("The exchange-rate service returned an incomplete response.");
        }
        LocalDate rateDate;
        try {
            rateDate = LocalDate.parse(rateResponse.date());
        } catch (RuntimeException exception) {
            throw new IOException("The exchange-rate service returned an invalid date.", exception);
        }
        return new ConversionResult(base, quote, amount, rateResponse.rate(),
                amount.multiply(rateResponse.rate()).setScale(2, RoundingMode.HALF_UP), rateDate);
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || !CURRENCY_CODE.matcher(currency.trim()).matches()) {
            throw new IllegalArgumentException("Currency codes must contain three letters.");
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private record RateResponse(String date, String base, String quote, BigDecimal rate) {
    }

    public record ConversionResult(String from, String to, BigDecimal amount, BigDecimal rate,
                                   BigDecimal convertedAmount, LocalDate rateDate) {
    }
}
