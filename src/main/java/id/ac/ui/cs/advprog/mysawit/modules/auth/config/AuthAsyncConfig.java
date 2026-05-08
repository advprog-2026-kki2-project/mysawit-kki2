package id.ac.ui.cs.advprog.mysawit.modules.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables Spring's asynchronous method execution (@Async) so that
 * notification event listeners in the auth module run on a background
 * thread without blocking the main request thread.
 */
@Configuration
@EnableAsync
public class AuthAsyncConfig {
}
