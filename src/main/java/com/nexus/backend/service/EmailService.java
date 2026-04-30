package com.nexus.backend.service;

import com.nexus.backend.model.Application;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            System.out.println("Email sent to: " + to);
        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Async
    public void sendApplicationReceived(Application app) {
        String recruiterEmail =
                app.getJob().getCreatedBy().getEmail();
        String recruiterName =
                app.getJob().getCreatedBy().getFullName();
        String candidateName =
                app.getCandidate().getFullName();
        String jobTitle = app.getJob().getTitle();
        int score = app.getMatchScore() != null
                ? app.getMatchScore() : 0;

        String scoreColor = score >= 70 ? "#10B981"
                : score >= 40 ? "#F59E0B" : "#EF4444";

        String html = buildEmail(
                "New Application Received",
                "Hi " + recruiterName + ",",
                "<strong>" + candidateName + "</strong> has applied for "
                        + "<strong>" + jobTitle + "</strong>."
                        + "<br><br>AI Match Score: "
                        + "<span style='color:" + scoreColor
                        + ";font-weight:800;font-size:20px'>"
                        + score + "%</span>",
                "View Application",
                frontendUrl + "/recruiter",
                "#9333EA"
        );
        sendEmail(recruiterEmail,
                "New Application — " + jobTitle, html);
    }
    @Async
    public void sendPasswordResetEmail(String to,
                                       String name, String resetLink) {
        String html = buildEmail(
                "Reset Your Password",
                "Hi " + name + ",",
                "You requested a password reset for your NEXUS account."
                        + "<br><br>Click the button below to reset your password."
                        + "<br>This link expires in <strong>30 minutes</strong>."
                        + "<br><br>If you did not request this, ignore this email.",
                "Reset Password",
                resetLink,
                "#9333EA"
        );
        sendEmail(to, "Reset Your NEXUS Password", html);
    }

    @Async
    public void sendInterviewInvitation(Application app) {
        String email = app.getCandidate().getEmail();
        String name  = app.getCandidate().getFullName();
        String job   = app.getJob().getTitle();

        String html = buildEmail(
                "You are invited for an Interview!",
                "Hi " + name + ",",
                "Congratulations! You have been shortlisted for an "
                        + "interview for the position of "
                        + "<strong>" + job + "</strong>."
                        + "<br><br>The hiring team will contact you with "
                        + "date and time details shortly.",
                "View Application Status",
                frontendUrl + "/candidate",
                "#9333EA"
        );
        sendEmail(email, "Interview Invitation — " + job, html);
    }

    @Async
    public void sendOfferEmail(Application app) {
        String email = app.getCandidate().getEmail();
        String name  = app.getCandidate().getFullName();
        String job   = app.getJob().getTitle();

        String html = buildEmail(
                "Congratulations! You got the job!",
                "Hi " + name + ",",
                "We are thrilled to inform you that you have been "
                        + "selected for the position of "
                        + "<strong>" + job + "</strong>."
                        + "<br><br>Welcome to the team! The HR team will "
                        + "reach out with your official offer letter shortly.",
                "View Your Application",
                frontendUrl + "/candidate",
                "#10B981"
        );
        sendEmail(email,
                "Job Offer — " + job + " 🎉", html);
    }

    @Async
    public void sendRejectionEmail(Application app) {
        String email = app.getCandidate().getEmail();
        String name  = app.getCandidate().getFullName();
        String job   = app.getJob().getTitle();

        String html = buildEmail(
                "Application Status Update",
                "Hi " + name + ",",
                "Thank you for your interest in the "
                        + "<strong>" + job + "</strong> position."
                        + "<br><br>After careful consideration, we have "
                        + "decided to move forward with other candidates "
                        + "at this time. We appreciate your effort and "
                        + "encourage you to apply for future openings.",
                "Browse Other Jobs",
                frontendUrl + "/candidate",
                "#9333EA"
        );
        sendEmail(email,
                "Application Update — " + job, html);
    }

    @Async
    public void sendScreeningEmail(Application app) {
        String email = app.getCandidate().getEmail();
        String name  = app.getCandidate().getFullName();
        String job   = app.getJob().getTitle();

        String html = buildEmail(
                "Your application is being reviewed!",
                "Hi " + name + ",",
                "Good news! Your application for "
                        + "<strong>" + job + "</strong> "
                        + "has moved to the screening stage."
                        + "<br><br>Our team is reviewing your profile. "
                        + "You will hear from us soon.",
                "Track Your Application",
                frontendUrl + "/candidate",
                "#9333EA"
        );
        sendEmail(email,
                "Application in Screening — " + job, html);
    }

    @Async
    public void sendFinalRoundEmail(Application app) {
        String email = app.getCandidate().getEmail();
        String name  = app.getCandidate().getFullName();
        String job   = app.getJob().getTitle();

        String html = buildEmail(
                "You are in the Final Round!",
                "Hi " + name + ",",
                "Excellent news! You have advanced to the "
                        + "<strong>Final Round</strong> for "
                        + "<strong>" + job + "</strong>."
                        + "<br><br>You are one step away from the offer. "
                        + "Stay confident and prepared!",
                "View Application",
                frontendUrl + "/candidate",
                "#EC4899"
        );
        sendEmail(email,
                "Final Round — " + job + " 🚀", html);
    }

    private String buildEmail(
            String heading,
            String greeting,
            String body,
            String btnText,
            String btnLink,
            String accentColor) {

        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8">
            <meta name="viewport" content="width=device-width"></head>
            <body style="margin:0;padding:0;
                background:#0D0B1A;
                font-family:'Inter',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0"
                  style="background:#0D0B1A;padding:40px 20px;">
                <tr><td align="center">
                  <table width="560" cellpadding="0" cellspacing="0"
                      style="background:#150F2B;
                      border:1px solid #2D1F5E;
                      border-radius:16px;overflow:hidden;">

                    <!-- Header -->
                    <tr>
                      <td style="background:linear-gradient(135deg,
                          #9333EA,#C026D3,#EC4899);
                          padding:28px 40px;">
                        <table width="100%%"><tr><td>
                          <span style="display:inline-block;
                              width:36px;height:36px;
                              background:rgba(255,255,255,0.2);
                              border-radius:8px;
                              text-align:center;line-height:36px;
                              font-weight:800;font-size:18px;
                              color:#fff;vertical-align:middle;
                              margin-right:10px;">N</span>
                          <span style="font-size:18px;font-weight:800;
                              color:#fff;vertical-align:middle;
                              letter-spacing:0.1em;">NEXUS</span>
                        </td></tr></table>
                      </td>
                    </tr>

                    <!-- Body -->
                    <tr>
                      <td style="padding:36px 40px;">
                        <h2 style="margin:0 0 8px;font-size:22px;
                            font-weight:800;color:#F0EEFF;">
                          %s
                        </h2>
                        <p style="margin:0 0 20px;font-size:15px;
                            color:#A89CC8;">
                          %s
                        </p>
                        <p style="margin:0 0 32px;font-size:15px;
                            color:#D4D0F0;line-height:1.7;">
                          %s
                        </p>
                        <a href="%s"
                            style="display:inline-block;
                            padding:13px 28px;
                            background:linear-gradient(135deg,
                            %s,#EC4899);
                            color:#ffffff;text-decoration:none;
                            border-radius:10px;font-size:15px;
                            font-weight:700;
                            box-shadow:0 4px 15px rgba(147,51,234,0.4);">
                          %s
                        </a>
                      </td>
                    </tr>

                    <!-- Footer -->
                    <tr>
                      <td style="padding:20px 40px;
                          border-top:1px solid #2D1F5E;">
                        <p style="margin:0;font-size:12px;
                            color:#6B5A9E;text-align:center;">
                          This email was sent by NEXUS AI Hiring Platform
                          <br>© 2026 NEXUS · BitSpark Technologies
                        </p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(
                heading, greeting, body,
                btnLink, accentColor, btnText
        );
    }
}