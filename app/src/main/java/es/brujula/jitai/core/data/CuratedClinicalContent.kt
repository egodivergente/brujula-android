package es.brujula.jitai.core.data

import es.brujula.jitai.core.model.BreathingPattern
import es.brujula.jitai.core.model.CopingExerciseCard
import es.brujula.jitai.core.model.CuratedGuidanceTopic

/**
 * Contenidos clínicos revisados, breves y serenos.
 * Sin juicios, sin diagnósticos ni sobrecarga de texto en momentos sensibles.
 */
object CuratedClinicalContent {

    val breathingPatterns = listOf(
        BreathingPattern(
            id = "physiological_sigh",
            title = "Suspiro suave",
            shortTag = "4 · 2 · 6",
            subtitle = "Suelta tensión rápidamente con una exhalación larga.",
            inhaleSec = 4,
            holdAfterInhaleSec = 2,
            exhaleSec = 6,
            holdAfterExhaleSec = 1,
            recommendedCycles = 5
        ),
        BreathingPattern(
            id = "box_breathing",
            title = "Equilibrio",
            shortTag = "4 · 4 · 4 · 4",
            subtitle = "Centra la mente cuando todo va demasiado deprisa.",
            inhaleSec = 4,
            holdAfterInhaleSec = 4,
            exhaleSec = 4,
            holdAfterExhaleSec = 4,
            recommendedCycles = 4
        ),
        BreathingPattern(
            id = "soft_anchor",
            title = "Descanso nocturno",
            shortTag = "4 · 0 · 7",
            subtitle = "Acompaña al cuerpo a bajar revoluciones.",
            inhaleSec = 4,
            holdAfterInhaleSec = 0,
            exhaleSec = 7,
            holdAfterExhaleSec = 1,
            recommendedCycles = 5
        )
    )

    val copingExercises = listOf(
        CopingExerciseCard(
            id = "urge_surfing_3m",
            title = "Dejar pasar la ola",
            durationLabel = "3 min",
            category = "Impulso intenso",
            summary = "Un impulso sube, alcanza su cresta y baja solo si le das unos minutos de aire.",
            steps = listOf(
                "Apoya los pies en el suelo y nota dónde sientes el impulso en el cuerpo.",
                "Nómbralo sin pelear: «es una ola pasajera, no tengo que decidir ahora».",
                "Suelta cinco exhalaciones lentas dejando caer los hombros.",
                "Elige solo qué harás los próximos diez minutos: beber agua, cambiar de luz o salir al balcón."
            )
        ),
        CopingExerciseCard(
            id = "grounding_54321",
            title = "Volver al cuerpo · 5 · 4 · 3",
            durationLabel = "2 min",
            category = "Ruido mental",
            summary = "Trae la atención a lo que te rodea cuando la cabeza se acelera.",
            steps = listOf(
                "Mira 5 colores o formas reales a tu alrededor.",
                "Toca 4 texturas cercanas (la ropa, tus manos, una superficie fresca).",
                "Escucha 3 sonidos suaves del ambiente.",
                "Elige 1 gesto de cuidado inmediato para tu cuerpo."
            )
        ),
        CopingExerciseCard(
            id = "safe_exit_plan",
            title = "Retirarse a tiempo",
            durationLabel = "90 s",
            category = "Tus límites",
            summary = "Tener lista una frase sencilla te da libertad para irte cuando algo deja de apetecerte.",
            steps = listOf(
                "Puedes cambiar de idea en cualquier momento sin justificarte.",
                "Frase lista: «Me retiro ya a descansar, gracias por el rato».",
                "Asegura batería, transporte de vuelta y avisa a alguien de confianza si te ayuda.",
                "Al llegar a casa: agua fresca, luz tenue y descanso."
            )
        ),
        CopingExerciseCard(
            id = "gentle_landing",
            title = "Aterrizar sin culpa",
            durationLabel = "3 min",
            category = "Cuidado posterior",
            summary = "Un tropiezo no borra tu camino. Hoy lo único importante es cuidar tu cuerpo y descansar.",
            steps = listOf(
                "Bebe agua o suero a pequeños sorbos.",
                "Baja el brillo de las pantallas, ventila la habitación y busca calma.",
                "Evita mezclar otras sustancias para compensar el cansancio.",
                "Cuando hayas dormido, podrás comentarlo sin juicio con tu enfermera si quieres."
            )
        )
    )

    val curatedGuidanceTopics = listOf(
        CuratedGuidanceTopic(
            id = "stimulants_care",
            title = "Calor, hidratación y pausas",
            category = "Cuidado físico",
            keywords = listOf("tina", "mefe", "metanfetamina", "mefedrona", "calor", "agua", "corazón", "taquicardia", "dormir"),
            reviewedResponse = "Durante sesiones largas es fácil no notar sed, calor ni cansancio. Hacer pausas breves en un espacio fresco protege tu cuerpo y te devuelve claridad.",
            keyTakeaways = listOf(
                "Hidrátate a sorbos regulares con agua o bebida con sales, sin beber litros de golpe.",
                "Ante dolor en el pecho, fiebre alta o falta de aire, llama al 112 sin esperar.",
                "Utiliza siempre material individual sin compartir."
            ),
            showEmergency112Banner = true
        ),
        CuratedGuidanceTopic(
            id = "depressants_alert",
            title = "Sedación intensa y mezclas de riesgo",
            category = "Señales de urgencia",
            keywords = listOf("mezcla", "alcohol", "ketamina", "sueño", "inconsciente", "despierta", "respirar", "sobredosis"),
            reviewedResponse = "Combinar sustancias sedantes con alcohol, pastillas para dormir o ketamina multiplica el riesgo de pérdida de conciencia y parada respiratoria.",
            keyTakeaways = listOf(
                "Evita mezclar depresores del sistema nervioso entre sí.",
                "Si alguien no despierta al moverle los hombros o respira muy lento: ponlo de lado y llama al 112 de inmediato.",
                "En urgencias médicas la prioridad es cuidar la vida."
            ),
            showEmergency112Banner = true
        ),
        CuratedGuidanceTopic(
            id = "sexual_health_prep_pep",
            title = "Salud sexual, PrEP y PEP",
            category = "Salud sexual",
            keywords = listOf("prep", "pep", "vih", "its", "condón", "preservativo", "lubricante", "analítica", "sexo", "deseo"),
            reviewedResponse = "Tu deseo sexual es legítimo y BRÚJULA nunca lo trata como un problema. Tu enfermera puede coordinar contigo PrEP, controles de ITS o valoración urgente de PEP.",
            keyTakeaways = listOf(
                "Si hubo riesgo de VIH sin PrEP activa, acude a urgencias hospitalarias cuanto antes (siempre antes de 72 h) para valorar PEP.",
                "Usa lubricante abundante para cuidar las mucosas.",
                "Pon recordatorios suaves fuera de la sesión si tomas PrEP."
            ),
            showEmergency112Banner = false
        ),
        CuratedGuidanceTopic(
            id = "consent_boundaries",
            title = "Consentimiento y acuerdos propios",
            category = "Autonomía",
            keywords = listOf("consentimiento", "límites", "acuerdos", "salir", "parar", "casa", "presión"),
            reviewedResponse = "El consentimiento es libre en cada momento y desaparece si alguien está muy sedado. Tener claros tus límites de antemano protege tu bienestar.",
            keyTakeaways = listOf(
                "Decide en calma qué te apetece y a qué hora quieres volver.",
                "Decir que no o marcharte siempre es válido.",
                "Tu centro sanitario ofrece atención confidencial si has vivido algo sin tu consentimiento."
            ),
            showEmergency112Banner = false
        ),
        CuratedGuidanceTopic(
            id = "slam_harm_reduction",
            title = "Prevención de infecciones en piel y venas",
            category = "Reducción de daños",
            keywords = listOf("slam", "slamming", "aguja", "jeringuilla", "vena", "infección", "fiebre", "piel"),
            reviewedResponse = "La vía inyectada eleva mucho el riesgo de infecciones y sobredosis. Si está presente, usar material estéril propio de un solo uso y limpiar la piel con alcohol es esencial.",
            keyTakeaways = listOf(
                "No compartas ni reutilices agujas, jeringuillas, filtros ni recipientes.",
                "Si notas calor local que crece, pus o fiebre, consulta en urgencias o con enfermería.",
                "Puedes hablar sin estigma con tu enfermera si deseas pasar a vías de menor riesgo."
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
