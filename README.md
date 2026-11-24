# Currency Converter

A Java Swing desktop app that converts currencies using the latest available rates from [Frankfurter](https://www.frankfurter.dev/). Rates are supplied by the external service and may not be real-time.

## Requirements

- Java 25 or newer
- Maven 3.6+
- Internet access for exchange-rate lookups

## Run

```shell
mvn compile exec:java -Dexec.mainClass=com.example.currencyconverter.CurrencyConverterApplication
```

Choose or enter three-letter currency codes, enter a non-negative amount, and select **Convert**. The converted amount is rounded to two decimal places.
