package service.carsharing.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {
    @Value("${STRIPE_SECRET_CODE}")
    private String apiKey;

    @PostConstruct
    public void setup() {
        Stripe.apiKey = apiKey;
    }
}
