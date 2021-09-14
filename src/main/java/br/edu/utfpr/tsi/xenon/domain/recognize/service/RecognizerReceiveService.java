package br.edu.utfpr.tsi.xenon.domain.recognize.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecognizerReceiveService {
    public static final float LIMIT_CONFIDENCE_VALID = 75.0F;


}
