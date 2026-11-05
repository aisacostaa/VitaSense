package br.vitasense.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Liga o agendador (@Scheduled) usado pelas automações por horário. */
@Configuration
@EnableScheduling
public class AgendamentoConfig {}
