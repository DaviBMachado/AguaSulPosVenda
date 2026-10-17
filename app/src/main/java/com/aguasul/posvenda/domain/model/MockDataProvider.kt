package com.Aquasul.posvenda.domain.model

/**
 * Fornece dados simulados (mocks) realistas para a carteira de clientes
 * e a agenda de visitas diárias da Aqua Sul.
 */
object MockDataProvider {

    val clientesIniciais: List<Cliente> = listOf(
        Cliente(
            id = "cli_001",
            nome = "Hospital São Lucas",
            contato = "(11) 98765-4321 - Dra. Amanda",
            andamentoUso = "Intensivo (24h)",
            usabilidade = "Excelente",
            reclamacoes = emptyList()
        ),
        Cliente(
            id = "cli_002",
            nome = "Condomínio Reserva das Aquas",
            contato = "(11) 97654-3210 - Síndico Roberto",
            andamentoUso = "Regular",
            usabilidade = "Boa",
            reclamacoes = listOf(
                "Baixa vazão no bloco B relatada em 05/10",
                "Ruído intermitente na bomba de recalque"
            )
        ),
        Cliente(
            id = "cli_003",
            nome = "Escola Municipal Monteiro Lobato",
            contato = "(11) 96543-2109 - Diretora Helena",
            andamentoUso = "Médio (horário escolar)",
            usabilidade = "Satisfatória",
            reclamacoes = emptyList()
        ),
        Cliente(
            id = "cli_004",
            nome = "Indústria Metalúrgica FerroAço",
            contato = "(11) 95432-1098 - Eng. Carlos",
            andamentoUso = "Crítico / Industrial",
            usabilidade = "Requer Calibração",
            reclamacoes = listOf(
                "Necessidade de reposição emergencial de refil desmineralizador"
            )
        ),
        Cliente(
            id = "cli_005",
            nome = "Restaurante Sabor Natural",
            contato = "(11) 94321-0987 - Chef Juliana",
            andamentoUso = "Alto",
            usabilidade = "Excelente",
            reclamacoes = emptyList()
        ),
        Cliente(
            id = "cli_006",
            nome = "Academia Corpo & Vida",
            contato = "(11) 93210-9876 - Gerente Rodrigo",
            andamentoUso = "Intensivo",
            usabilidade = "Boa",
            reclamacoes = listOf(
                "Gosto alterado na Aqua do bebedouro 2"
            )
        )
    )

    val visitasIniciais: List<Visita> = listOf(
        Visita(
            id = "vis_001",
            clienteId = "cli_001",
            clienteNome = "Hospital São Lucas",
            dataHorario = "Hoje às 08:30",
            tipo = Visita.TIPO_PREVENTIVA,
            status = Visita.STATUS_PENDENTE
        ),
        Visita(
            id = "vis_002",
            clienteId = "cli_002",
            clienteNome = "Condomínio Reserva das Aquas",
            dataHorario = "Hoje às 10:45",
            tipo = Visita.TIPO_CORRETIVA,
            status = Visita.STATUS_PENDENTE
        ),
        Visita(
            id = "vis_003",
            clienteId = "cli_004",
            clienteNome = "Indústria Metalúrgica FerroAço",
            dataHorario = "Hoje às 14:00",
            tipo = Visita.TIPO_TROCA_FILTRO,
            status = Visita.STATUS_PENDENTE
        ),
        Visita(
            id = "vis_004",
            clienteId = "cli_005",
            clienteNome = "Restaurante Sabor Natural",
            dataHorario = "Hoje às 16:30",
            tipo = Visita.TIPO_TREINAMENTO,
            status = Visita.STATUS_PENDENTE
        )
    )
}
