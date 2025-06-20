package com.example.demoLTI13.controller;

import com.example.demoLTI13.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Controller
public class LtiController {
    private final String moodleIssuer = "http://localhost:8100";
    private final String moodleAuthEndpoint = moodleIssuer + "/mod/lti/auth.php";

    private final List<String> validIssuers = List.of(
            "http://localhost:8100",
            "http://127.0.0.1:8100",
            "https://localhost:8100",
            "http://localhost:8100/"
    );

    @RequestMapping(value = "/lti/login", method = {RequestMethod.GET, RequestMethod.POST})
    public RedirectView initiateLogin(
            @RequestParam("iss") String issuer,
            @RequestParam("login_hint") String loginHint,
            @RequestParam("target_link_uri") String targetLinkUri,
            @RequestParam("lti_message_hint") String ltiMessageHint,
            @RequestParam("client_id") String clientId,
            @RequestParam("lti_deployment_id") String deploymentId) {

        System.out.println("=== LTI Login Started ===");
        System.out.println("Issuer: " + issuer);
        System.out.println("Login Hint: " + loginHint);
        System.out.println("Target Link URI: " + targetLinkUri);
        System.out.println("LTI Message Hint: " + ltiMessageHint);
        System.out.println("Client ID: " + clientId);
        System.out.println("Deployment ID: " + deploymentId);

        if (!validIssuers.contains(issuer)) {
            System.err.println("Invalid issuer: " + issuer);
            throw new IllegalArgumentException("Invalid issuer: " + issuer);
        }

        String encodedLtiMessageHint = URLEncoder.encode(ltiMessageHint, StandardCharsets.UTF_8);

        String redirectUrl = String.format(
                "%s?scope=openid&response_type=id_token&response_mode=form_post" +
                        "&client_id=%s&redirect_uri=%s&login_hint=%s&lti_message_hint=%s" +
                        "&state=%s&nonce=%s",
                moodleAuthEndpoint, clientId, "https://fc38-2405-4802-9060-34f0-646c-77b-886c-997.ngrok-free.app/lti/launch",
                loginHint, encodedLtiMessageHint, generateState(), generateNonce()
        );

        System.out.println("Redirecting to: " + redirectUrl);
        return new RedirectView(redirectUrl);
    }

    private String generateState() {
        String state = java.util.UUID.randomUUID().toString();
        System.out.println("Generated State: " + state);
        return state;
    }

    private String generateNonce() {
        String nonce = java.util.UUID.randomUUID().toString();
        System.out.println("Generated Nonce: " + nonce);
        return nonce;
    }

    @RequestMapping(value = "/lti/launch", method = {RequestMethod.GET, RequestMethod.POST})
    public String handleLaunch(@RequestParam("id_token") String idToken) {
        System.out.println("=== LTI Launch Started ===");
        System.out.println("Received id_token: " + idToken);

        try {
            System.out.println("Parsing JWT via JWKS...");
            Claims body = JwtUtil.validate(idToken);
            System.out.println("JWT valid. Body: " + body);

            if (!validIssuers.contains(body.getIssuer())) {
                throw new IllegalArgumentException("Invalid issuer: " + body.getIssuer());
            }

            System.out.println("sub = " + body.getSubject());

            // Retrieve 'sub' as String
            String userId = body.getSubject();

            // Retrieve 'context' as Map
            Map<String, Object> context = body.get("https://purl.imsglobal.org/spec/lti/claim/context", Map.class);
            System.out.println("Context ID: " + context.get("id"));
            System.out.println("Context Label: " + context.get("label"));
            System.out.println("Context Title: " + context.get("title"));
            System.out.println("Context Type: " + context.get("type"));

            // Retrieve 'roles' as List
            List<String> roles = body.get("https://purl.imsglobal.org/spec/lti/claim/roles", List.class);
            System.out.println("Roles: " + roles);

            System.out.println("Full JWT Claims: " + body);

            System.out.println("Redirecting to: /lti/welcome");
            return "launch";

        } catch (JwtException e) {
            System.err.println("JWT Parsing Error: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Returning error page due to JWT failure");
            return "error";
        } catch (Exception e) {
            System.err.println("Unexpected Error: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Returning error page due to unexpected error");
            return "error";
        }
    }

    @RequestMapping(value = "/lti/welcome", method = {RequestMethod.GET, RequestMethod.POST})
    public String showWelcomePage() {
        System.out.println("Rendering welcome page: launch.html");
        return "launch";
    }

    @RequestMapping(value = "/error", method = {RequestMethod.GET, RequestMethod.POST})
    public String handleError() {
        System.out.println("Rendering error page: error.html");
        return "error";
    }
}