package com.djio.grover_hospital.security;

/** Parsed claims from a result-download JWT. */
public record ResultDownloadToken(Long patientId, Long resultId, Long fileId) {}