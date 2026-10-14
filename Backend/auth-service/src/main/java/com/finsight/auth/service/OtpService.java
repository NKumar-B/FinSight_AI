package com.finsight.auth.service;

import com.finsight.auth.dto.request.ForgotPasswordRequest;
import com.finsight.auth.dto.request.ResetPasswordRequest;
import com.finsight.auth.dto.request.VerifyOtpRequest;
import com.finsight.auth.dto.response.OtpResponse;

public interface OtpService {

    OtpResponse generateAndSendOtp(ForgotPasswordRequest request);

    OtpResponse verifyOtp(VerifyOtpRequest request);

    void resetPassword(ResetPasswordRequest request);
}
