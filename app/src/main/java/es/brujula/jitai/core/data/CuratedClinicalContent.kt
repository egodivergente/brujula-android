package es.brujula.jitai.core.data

import es.brujula.jitai.core.model.BreathingPattern
import es.brujula.jitai.core.model.CopingExerciseCard
import es.brujula.jitai.core.model.CuratedGuidanceTopic

/**
 * Contenidos clínicos revisados de afrontamiento, salud sexual y reducción de daños (Parte 2 · §2.4 y §2.5).
 * Tono cercano, respetuoso, libre de juicios y estigma. Sin recomendaciones de dosis ni diagnósticos.
 */
object CuratedClinicalContent {

    val breathingPatterns = listOf(
        BreathingPattern(
            id = "physiological_sigh",
            title = "Suspiro fisiológico 4 · 2 · 6",
            subtitle = "Exhalación prolongada para reducir activación autonómica en menos de 2 minutos.",
            inhaleSec = 4,
            holdAfterInhaleSec = 2,
            exhaleSec = 6,
            holdAfterExhaleSec = 1,
            recommendedCycles = 6
        ),
        BreathingPattern(
            id = "box_breathing",
            title = "Cadencia cuadrada 4 · 4 · 4 · 4",
            subtitle = "Estabiliza el foco atencional cuando sientes aceleración interna o ruido mental.",
            inhaleSec = 4,
            holdAfterInhaleSec = 4,
            exhaleSec = 4,
            holdAfterExhaleSec = 4,
            recommendedCycles = 5
        ),
        BreathingPattern(
            id = "soft_anchor",
            title = "Descenso nocturno 4 · 0 · 7",
            subtitle = "Pensado para aterrizar el cuerpo antes de descansar o tras horas sin sueño.",
            inhaleSec = 4,
            holdAfterInhaleSec = 0,
            exhaleSec = 7,
            holdAfterExhaleSec = 1,
            recommendedCycles = 6
        )
    )

    val copingExercises = listOf(
        CopingExerciseCard(
            id = "urge_surfing_3m",
            title = "Surfear la ola (3 minutos)",
            durationLabel = "3 min · Afrontamiento del craving",
            category = "Craving intenso",
            summary = "Observar el impulso como una ola fisiológica que sube, alcanza una cresta y desciende sin necesidad de obedecerla ni pelear contra ella.",
            steps = listOf(
                "Apoya ambos pies en el suelo y localiza en qué parte del cuerpo notas el impulso (pecho, garganta, estómago, mandíbula).",
                "Ponle temperatura y forma sin juzgarte: «estoy notando una ola de deseo de consumir, no es una orden irreversible».",
                "Acompaña 5 exhalaciones lentas dejando caer los hombros. Registra si la intensidad baja aunque sea un punto en 20 minutos.",
                "Elige solo los próximos 15 minutos: beber agua fresca, cambiar de habitación o escribir a alguien de confianza."
            )
        ),
        CopingExerciseCard(
            id = "grounding_54321",
            title = "Anclaje sensorial 5 · 4 · 3 · 2 · 1",
            durationLabel = "2 min · Presencia física",
            category = "Desconexión / ansiedad",
            summary = "Devuelve la atención al entorno inmediato cuando aparece rumiación, sobrecarga estimular o sensación de pérdida de timón.",
            steps = listOf(
                "Nombra mentalmente 5 objetos reales que veas ahora mismo con sus colores y sombras.",
                "Toca 4 texturas distintas cercanas (tela, mesa fría, piel de tus manos, pared).",
                "Identifica 3 sonidos presentes en el ambiente, lejanos o cercanos.",
                "Reconoce 2 aromas o la temperatura del aire al entrar por la nariz.",
                "Elige 1 gesto amable inmediato hacia tu cuerpo (hidratarte, abrigarte o sentarte cómodo)."
            )
        ),
        CopingExerciseCard(
            id = "safe_exit_plan",
            title = "Guion de salida cuidada",
            durationLabel = "90 s · Autonomía en contexto social",
            category = "Límites y cuidado",
            summary = "Tener decidida una frase breve y neutra facilita retirarte a tiempo cuando notas que una situación deja de sentarte bien.",
            steps = listOf(
                "Recuerda que puedes cambiar de opinión o terminar un encuentro en cualquier momento sin dar explicaciones largas.",
                "Frase sencilla lista: «Me voy a retirar ya a descansar, gracias por el rato».",
                "Ten a mano transporte de vuelta, batería suficiente y avisa a tu contacto de referencia si lo necesitas.",
                "Al llegar a un espacio tranquilo, prioriza agua, algo suave de comer, ducha templada y oscuridad."
            )
        ),
        CopingExerciseCard(
            id = "gentle_landing",
            title = "Aterrizaje sin culpa tras un episodio",
            durationLabel = "3 min · Recuperación",
            category = "Autocompasión clínica",
            summary = "Un episodio de consumo no borra tu proceso ni tus herramientas. Lo prioritario ahora es cuidar el cuerpo y descansar.",
            steps = listOf(
                "Hidrátate a sorbos pequeños (agua o suero oral) sin forzar grandes cantidades de golpe.",
                "Reduce luz, pantallas y estímulos intensos; ventila la estancia si sientes calor.",
                "Evita añadir otras sustancias para compensar el bajón sin valoración médica.",
                "Cuando hayas descansado, si te apetece, anota con calma qué te ayudaría la próxima vez junto a tu enfermera."
            )
        )
    )

    val curatedGuidanceTopics = listOf(
        CuratedGuidanceTopic(
            id = "ghb_gbl_risks",
            title = "GHB / GBL · Señales de alerta y mezclas de alto riesgo",
            category = "Reducción de daños",
            keywords = listOf("ghb", "gbl", "bote", "g", "doblar", "sueño", "alcohol", "ketamina", "inconsciente", "sobredosis"),
            reviewedResponse = "El GHB/GBL tiene un margen muy estrecho entre el efecto buscado y la depresión respiratoria o pérdida de conciencia. El riesgo aumenta de forma crítica si se combina con otros depresores del sistema nervioso central (alcohol, benzodiacepinas, opioides o ketamina). BRÚJULA incluye un cronómetro voluntario únicamente para que veas cuánto tiempo ha pasado desde tu último registro, pero ningún reloj digital garantiza que repetir sea seguro.",
            keyTakeaways = listOf(
                "Evita mezclar GHB/GBL con alcohol, pastillas para dormir o ketamina.",
                "Si una persona no despierta al hablarle o moverle los hombros, respira lento/irregular o vomita adormilada: colócala de lado (posición lateral de seguridad) y llama al 112 de inmediato.",
                "En urgencias sanitarias prima salvar la vida; explica con claridad qué se ha tomado."
            ),
            showEmergency112Banner = true
        ),
        CuratedGuidanceTopic(
            id = "stimulants_care",
            title = "Estimulantes (metanfetamina / mefedrona) · Calor, hidratación y descanso",
            category = "Reducción de daños",
            keywords = listOf("tina", "mefe", "metanfetamina", "mefedrona", "calor", "agua", "corazón", "taquicardia", "dormir", "mandíbula"),
            reviewedResponse = "Con metanfetamina o mefedrona es frecuente perder la percepción de sed, hambre, temperatura corporal y paso de las horas. Hacer pausas periódicas en un lugar fresco ayuda a proteger el sistema cardiovascular y a recuperar perspectiva sobre lo que realmente te apetece hacer.",
            keyTakeaways = listOf(
                "Bebe agua o bebida isotónica a sorbos regulares (aprox. 250–400 ml/hora si hay actividad física o calor), evitando tanto la deshidratación como beber litros seguidos.",
                "Si aparece dolor opresivo en el pecho, confusión intensa, fiebre alta o dificultad para respirar, solicita asistencia urgente en el 112.",
                "No compartas tubos, rulos ni material de administración para prevenir transmisión de VHC y otras infecciones."
            ),
            showEmergency112Banner = true
        ),
        CuratedGuidanceTopic(
            id = "sexual_health_prep_pep",
            title = "Salud sexual sin estigma · PrEP, PEP y controles periódicos",
            category = "Salud sexual",
            keywords = listOf("prep", "pep", "vih", "its", "condón", "preservativo", "lubricante", "analítica", "sexo", "deseo"),
            reviewedResponse = "El deseo sexual es legítimo y BRÚJULA nunca lo trata como un síntoma. Si mantienes prácticas sexuales en contextos de chemsex o fuera de ellos, tu enfermera navegadora puede coordinar contigo revisiones periódicas de ITS, acceso o ajuste de PrEP y valoración urgente de profilaxis post-exposición (PEP).",
            keyTakeaways = listOf(
                "Si ha habido una práctica con riesgo potencial de VIH y no tomas PrEP activa, la PEP es más eficaz cuanto antes se inicie (idealmente primeras horas y siempre antes de 72 h en urgencias hospitalarias).",
                "Usa abundante lubricante hidrosoluble o de silicona para proteger mucosas durante encuentros prolongados.",
                "Si tomas PrEP oral diaria o a demanda, programar recordatorios fuera de la sesión ayuda a no olvidar tomas."
            ),
            showEmergency112Banner = false
        ),
        CuratedGuidanceTopic(
            id = "consent_boundaries",
            title = "Consentimiento, acuerdos previos y cuidado mutuo",
            category = "Autonomía y límites",
            keywords = listOf("consentimiento", "límites", "acuerdos", "salir", "parar", "casa", "miedo", "presión"),
            reviewedResponse = "El consentimiento es específico para cada práctica y para cada persona, y deja de existir si alguien está muy sedado o pierde la conciencia. Acordar tus propios límites cuando estás descansado te permite consultarlos rápido si el contexto se vuelve confuso.",
            keyTakeaways = listOf(
                "Dejar por escrito tus límites antes de quedar (qué sí, qué no y a qué hora quieres volver) refuerza tu timón personal.",
                "Nadie te debe hacer sentir culpable por parar, decir que no o marcharte antes.",
                "Si has vivido una situación sin tu consentimiento, tu centro sanitario y los servicios especializados ofrecen atención confidencial médica y psicológica."
            ),
            showEmergency112Banner = false
        ),
        CuratedGuidanceTopic(
            id = "slam_harm_reduction",
            title = "Vía inyectada (slam) · Prevención de infecciones y venas",
            category = "Reducción de daños",
            keywords = listOf("slam", "slamming", "aguja", "jeringuilla", "vena", "infección", "fiebre", "piel"),
            reviewedResponse = "Si se utiliza la vía inyectada, el riesgo de infecciones bacterianas cutáneas, endocarditis, VIH y hepatitis C y de sobredosis rápida se multiplica. Utilizar siempre material estéril de un solo uso y extremar la higiene de manos y piel con toallita con alcohol es esencial.",
            keyTakeaways = listOf(
                "Nunca reutilices ni compartas agujas, jeringuillas, filtros, cazoletas ni agua de dilución (identifica tu propio material con colores).",
                "Si aparece enrojecimiento caliente que crece, pus o fiebre en las horas o días posteriores, consulta con enfermería o urgencias sin esperar.",
                "Si te planteas pasar a otra vía de menor riesgo o reducir frecuencia, puedes trabajarlo paso a paso con tu enfermera."
            ),
            showEmergency112Banner = false
        )
    )

    private val emergencyKeywords = listOf(
        "no respira", "no despierta", "inconsciente", "desmayo", "convulsión", "convulsion",
        "dolor pecho", "infarto", "suicidio", "quitarme la vida", "morir", "sobredosis", "112", "urgencia"
    )

    fun queryReviewedGuidance(userQuery: String): Pair<Boolean, List<CuratedGuidanceTopic>> {
        val normalized = userQuery.trim().lowercase()
        if (normalized.isBlank()) {
            return false to curatedGuidanceTopics
        }
        val urgent = emergencyKeywords.any { normalized.contains(it) }
        val matched = curatedGuidanceTopics.filter { topic ->
            topic.title.lowercase().contains(normalized) ||
                topic.category.lowercase().contains(normalized) ||
                topic.keywords.any { kw -> normalized.contains(kw) || kw.contains(normalized) }
        }
        return urgent to (if (matched.isEmpty()) curatedGuidanceTopics.take(2) else matched)
    }
}
