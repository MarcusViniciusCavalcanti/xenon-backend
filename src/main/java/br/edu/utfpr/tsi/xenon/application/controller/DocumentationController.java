package br.edu.utfpr.tsi.xenon.application.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
public class DocumentationController {

    @GetMapping(value = "/docs", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String documentation() {
        log.info("Recebendo requisição para documentação.");
        var resourceAsStream =
            this.getClass().getResourceAsStream("/public/redoc-static.html");

        return new BufferedReader(
            new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n"));
    }
}
