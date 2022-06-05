package mnu

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
class EmailSender(
    @Value("\${spring.mail.username}")
    private val from: String
) {
    @Autowired
    lateinit var emailSender: JavaMailSender

    fun sendMessage(to: String, subject: String, text: String) {
        val message = SimpleMailMessage()
        message.setTo(to)
        message.setFrom(from)
        message.setSubject(subject)
        message.setText(text)
        emailSender.send(message)
    }
}