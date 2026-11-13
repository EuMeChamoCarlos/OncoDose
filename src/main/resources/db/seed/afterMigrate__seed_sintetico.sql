-- =====================================================================
-- oncotimizeAPI - seed sintético (só profile dev; callback afterMigrate)
-- Idempotente: só insere o que falta; nunca sobrescreve dado existente.
--
-- Fontes:
--   REAL     = Gê et al. (2023): vetores do Fluorouracil (V, C, E, D) e
--              frascos/custos da Gencitabina (200 mg US$ 16, 1 g US$ 48).
--   CEP      = Figura 1 do projeto do CEP (códigos PRE2026-000123..130).
--   TASY     = só o formato e os nomes comerciais das apresentações, como
--              aparecem no Tasy. Nenhuma linha vem de paciente real.
--   FICTÍCIO = valores inventados para demonstração (custos, estoques,
--              doses, datas, códigos e apresentações que os documentos não trazem).
-- =====================================================================

-- Medicamentos (nomes/códigos iguais aos já cadastrados no banco de dev).
INSERT INTO medicamento (nome, codigo_interno) VALUES
    ('Fluorouracil',   'MED001'),
    ('Paclitaxel',     'MED002'),
    ('Carboplatin',    'MED003'),
    ('Gemcitabina',    'MED004'),
    ('Ciclofosfamida', 'MED008'),
    ('Oxaliplatina',   'MED009'),
    ('Docetaxel',      'MED010'),
    ('Irinotecano',    'MED011'),
    ('Trastuzumabe',   'MED012'),
    ('Cabazitaxel',    'MED015'),
    ('Doxorrubicina',  'MED016')
ON CONFLICT DO NOTHING;

-- Apresentações: (medicamento, volume mg, custo US$, estoque).
INSERT INTO apresentacao_frasco (medicamento_id, volume_mg, custo, quantidade_estoque)
SELECT m.id, v.volume, v.custo, v.estoque
FROM (VALUES
    ('Fluorouracil',    250,   74, 20),   -- REAL
    ('Fluorouracil',    500,  126, 30),   -- REAL
    ('Fluorouracil',   1000,  400, 50),   -- REAL
    ('Gemcitabina',     200,   16, 20),   -- REAL (estoque FICTÍCIO)
    ('Gemcitabina',    1000,   48, 20),   -- REAL (estoque FICTÍCIO)
    ('Paclitaxel',       30,   25, 20),   -- FICTÍCIO
    ('Paclitaxel',      100,   70, 20),   -- FICTÍCIO
    ('Paclitaxel',      300,  190, 10),   -- FICTÍCIO
    ('Carboplatin',      50,   15, 20),   -- FICTÍCIO
    ('Carboplatin',     150,   40, 20),   -- FICTÍCIO
    ('Carboplatin',     450,  110, 10),   -- FICTÍCIO
    ('Ciclofosfamida',  200,    8, 20),   -- FICTÍCIO
    ('Ciclofosfamida', 1000,   30, 10),   -- FICTÍCIO
    ('Oxaliplatina',     50,   60, 20),   -- FICTÍCIO
    ('Oxaliplatina',    100,  110, 20),   -- FICTÍCIO
    ('Docetaxel',        20,   45, 20),   -- FICTÍCIO
    ('Docetaxel',        80,  160, 20),   -- FICTÍCIO
    ('Irinotecano',      40,   20, 20),   -- FICTÍCIO
    ('Irinotecano',     100,   45, 20),   -- FICTÍCIO
    ('Trastuzumabe',    150,  300, 10),   -- FICTÍCIO
    ('Trastuzumabe',    440,  820, 10),   -- FICTÍCIO
    ('Cabazitaxel',      60, 1500,  5),   -- FICTÍCIO
    ('Doxorrubicina',    10,   30, 20),   -- FICTÍCIO
    ('Doxorrubicina',    50,  120, 20)    -- TASY: apresentação; custo e estoque FICTÍCIOS
) AS v(nome, volume, custo, estoque)
JOIN medicamento m ON m.nome = v.nome
WHERE NOT EXISTS (
    SELECT 1 FROM apresentacao_frasco f WHERE f.medicamento_id = m.id AND f.volume_mg = v.volume
);

-- Prescrições anonimizadas (LGPD): só código, medicamento, dose e status.
INSERT INTO prescricao (codigo_prescricao, medicamento_id, dose_mg, status, data_prescricao)
SELECT v.codigo, m.id, v.dose, v.status, v.data::date
FROM (VALUES
    -- REAL: Fluorouracil 01/11/2019, D = [300, 570, 490, 1270] → ótimo US$ 704.
    ('PRE2019-000001', 'Fluorouracil', 300,  'APTO',     '2019-11-01'),
    ('PRE2019-000002', 'Fluorouracil', 570,  'APTO',     '2019-11-01'),
    ('PRE2019-000003', 'Fluorouracil', 490,  'APTO',     '2019-11-01'),
    ('PRE2019-000004', 'Fluorouracil', 1270, 'APTO',     '2019-11-01'),
    ('PRE2019-000005', 'Fluorouracil', 5000, 'NAO_APTO', '2019-11-01'),  -- FICTÍCIO: mostra que NAO_APTO fica de fora
    -- Gencitabina 08/11/2019: total REAL de 3488 mg; divisão entre pacientes FICTÍCIA → ótimo US$ 192.
    ('PRE2019-000011', 'Gemcitabina', 1200, 'APTO', '2019-11-08'),
    ('PRE2019-000012', 'Gemcitabina', 1288, 'APTO', '2019-11-08'),
    ('PRE2019-000013', 'Gemcitabina', 1000, 'APTO', '2019-11-08'),
    -- CEP (Figura 1): doses reais do documento; distribuição das datas 02–05/04/2026 FICTÍCIA.
    ('PRE2026-000123', 'Ciclofosfamida', 1000, 'APTO', '2026-04-02'),
    ('PRE2026-000124', 'Paclitaxel',      260, 'APTO', '2026-04-02'),
    ('PRE2026-000125', 'Oxaliplatina',    200, 'APTO', '2026-04-03'),
    ('PRE2026-000126', 'Docetaxel',       120, 'APTO', '2026-04-03'),
    ('PRE2026-000127', 'Cabazitaxel',      45, 'APTO', '2026-04-04'),
    ('PRE2026-000128', 'Irinotecano',     180, 'APTO', '2026-04-04'),
    ('PRE2026-000129', 'Carboplatin',     450, 'APTO', '2026-04-05'),
    ('PRE2026-000130', 'Trastuzumabe',    420, 'APTO', '2026-04-05'),
    -- FICTÍCIO: mais pacientes de Paclitaxel em 02/04 para mostrar o compartilhamento de frascos.
    ('PRE2026-000131', 'Paclitaxel',      175, 'APTO', '2026-04-02'),
    ('PRE2026-000132', 'Paclitaxel',       80, 'APTO', '2026-04-02'),
    ('PRE2026-000133', 'Paclitaxel',      135, 'APTO', '2026-04-02')
) AS v(codigo, nome, dose, status, data)
JOIN medicamento m ON m.nome = v.nome
ON CONFLICT (codigo_prescricao, medicamento_id) DO NOTHING;

-- Protocolos (FICTÍCIO): cenário da agenda. Paclitaxel a cada 21 dias (mínimo 19): 9 protocolos têm
-- aplicação em 27/10/2026 e o PTC2026-000010 cai sozinho em 26/10 (3ª aplicação), com janela
-- de +1 dia — o planejador deve sugerir juntá-lo ao grupo de 27/10.
INSERT INTO protocolo_paciente (codigo_protocolo, medicacao_subtipo, data_inicio, ciclos,
                                dias_intervalo, intervalo_minimo_dias)
SELECT v.codigo, 'Paclitaxel', v.inicio::date, v.total, 21, 19
FROM (VALUES
    ('PTC2026-000001', '2026-10-27', 6),   -- 27/10 = 1ª aplicação
    ('PTC2026-000002', '2026-10-06', 6),   -- 27/10 = 2ª
    ('PTC2026-000003', '2026-09-15', 6),   -- 27/10 = 3ª
    ('PTC2026-000004', '2026-08-25', 6),   -- 27/10 = 4ª
    ('PTC2026-000005', '2026-10-27', 4),
    ('PTC2026-000006', '2026-10-06', 4),
    ('PTC2026-000007', '2026-09-15', 6),
    ('PTC2026-000008', '2026-10-27', 6),
    ('PTC2026-000009', '2026-10-06', 4),
    ('PTC2026-000010', '2026-09-14', 6)    -- 14/09, 05/10, 26/10 (isolado), 16/11...
) AS v(codigo, inicio, total)
ON CONFLICT (codigo_protocolo) DO NOTHING;

INSERT INTO protocolo_medicamento (protocolo_id, medicamento_id, dose_mg, via, dias_aplicacao)
SELECT p.id, m.id, v.dose, 'IV', 'D1'
FROM (VALUES
    ('PTC2026-000001', 260), ('PTC2026-000002', 175), ('PTC2026-000003',  80),
    ('PTC2026-000004', 135), ('PTC2026-000005', 150), ('PTC2026-000006', 210),
    ('PTC2026-000007',  95), ('PTC2026-000008', 120), ('PTC2026-000009', 190),
    ('PTC2026-000010', 175)
) AS v(codigo, dose)
JOIN protocolo_paciente p ON p.codigo_protocolo = v.codigo
JOIN medicamento m ON m.nome = 'Paclitaxel'
ON CONFLICT (protocolo_id, medicamento_id) DO NOTHING;

-- Aplicações projetadas: data_prevista = data_inicio + (n-1)·intervalo; agendada começa igual.
INSERT INTO aplicacao (protocolo_id, numero, ciclo_numero, dia_ciclo, data_prevista, data_agendada,
                       dias_antes, dias_depois, status)
SELECT p.id, n, n, 1, p.data_inicio + (n - 1) * p.dias_intervalo, p.data_inicio + (n - 1) * p.dias_intervalo,
       0, CASE WHEN p.codigo_protocolo = 'PTC2026-000010' THEN 1 ELSE 0 END, 'AGENDADA'
FROM protocolo_paciente p
CROSS JOIN LATERAL generate_series(1, p.ciclos) AS n
WHERE p.codigo_protocolo BETWEEN 'PTC2026-000001' AND 'PTC2026-000010'
ON CONFLICT DO NOTHING;

-- Prescrições do cenário (FICTÍCIO): liberam as aplicações de 26 e 27/10, emitidas no próprio dia,
-- como no fluxo real: a prescrição nasce depois da triagem e da avaliação médica.
-- Código derivado do protocolo (PTC2026-000007 → PRE2026-000207); sem alvo no ON CONFLICT para
-- cobrir também o UNIQUE de aplicação e medicamento.
INSERT INTO prescricao (codigo_prescricao, medicamento_id, dose_mg, status, data_prescricao, aplicacao_id)
SELECT 'PRE2026-0002' || right(p.codigo_protocolo, 2), pm.medicamento_id, pm.dose_mg, 'APTO', a.data_prevista, a.id
FROM aplicacao a
JOIN protocolo_paciente p ON p.id = a.protocolo_id
JOIN protocolo_medicamento pm ON pm.protocolo_id = p.id
WHERE p.codigo_protocolo BETWEEN 'PTC2026-000001' AND 'PTC2026-000010'
  AND a.data_prevista IN (DATE '2026-10-26', DATE '2026-10-27')
ON CONFLICT DO NOTHING;

-- =====================================================================
-- Cenário no formato do Tasy (TASY + FICTÍCIO). Estrutura de TAX W e AC como nas telas Protocolos,
-- Medicamentos e Ciclos; pacientes, doses, datas e códigos inventados. Doses = dose/m² × superfície
-- corporal fictícia: TAX W 80 mg/m²; AC doxorrubicina 60 mg/m² e ciclofosfamida 600 mg/m².
-- Em 2027 para não mudar o cenário da agenda acima.
-- =====================================================================

-- Nome comercial da apresentação, como na coluna Medicamento do Tasy.
UPDATE apresentacao_frasco f
SET descricao_tasy = v.descricao
FROM (VALUES
    ('Paclitaxel',     300, 'Paclitaxel 6mg/ml Frasco-ampola 50ml'),
    ('Doxorrubicina',   50, 'Cloridrato de Doxorrubicina 50mg Frasco-ampola'),
    ('Ciclofosfamida', 1000, 'Ciclofosfamida 25mg/mL Frasco-ampola 40mL')
) AS v(nome, volume, descricao)
JOIN medicamento m ON m.nome = v.nome
WHERE f.medicamento_id = m.id AND f.volume_mg = v.volume AND f.descricao_tasy IS NULL;

INSERT INTO protocolo_paciente (codigo_protocolo, medicacao_subtipo, grupo, data_inicio, ciclos,
                                dias_intervalo, intervalo_minimo_dias)
VALUES
    ('PTC2027-000001', 'TAX W', 'Sistema Reprodutivo Feminino', DATE '2027-03-01', 1,  7,  5),
    ('PTC2027-000002', 'TAX W', 'Sistema Reprodutivo Feminino', DATE '2027-03-01', 1,  7,  5),
    ('PTC2027-000003', 'TAX W', 'Sistema Reprodutivo Feminino', DATE '2027-03-02', 1,  7,  5),
    ('PTC2027-000004', 'AC',    'Sistema Reprodutivo Feminino', DATE '2027-03-01', 4, 21, 19),
    ('PTC2027-000005', 'AC',    'Sistema Reprodutivo Feminino', DATE '2027-03-01', 4, 21, 19),
    ('PTC2027-000006', 'AC',    'Sistema Reprodutivo Feminino', DATE '2027-03-01', 4, 21, 19)
ON CONFLICT (codigo_protocolo) DO NOTHING;

INSERT INTO protocolo_medicamento (protocolo_id, medicamento_id, dose_mg, via, dias_aplicacao)
SELECT p.id, m.id, v.dose, 'IV', v.dias
FROM (VALUES
    ('PTC2027-000001', 'Paclitaxel',     128, 'D1,D8,D15,D22,D29,D36,D43,D50,D57,D64,D71,D78'),  -- SC 1,60
    ('PTC2027-000002', 'Paclitaxel',     140, 'D1,D8,D15,D22,D29,D36,D43,D50,D57,D64,D71,D78'),  -- SC 1,75
    ('PTC2027-000003', 'Paclitaxel',     120, 'D1,D8,D15,D22,D29,D36,D43,D50,D57,D64,D71,D78'),  -- SC 1,50
    ('PTC2027-000004', 'Doxorrubicina',  102, 'D1'),                                             -- SC 1,70
    ('PTC2027-000004', 'Ciclofosfamida', 1020, 'D1'),
    ('PTC2027-000005', 'Doxorrubicina',  111, 'D1'),                                             -- SC 1,85
    ('PTC2027-000005', 'Ciclofosfamida', 1110, 'D1'),
    ('PTC2027-000006', 'Doxorrubicina',   99, 'D1'),                                             -- SC 1,65
    ('PTC2027-000006', 'Ciclofosfamida',  990, 'D1')
) AS v(codigo, nome, dose, dias)
JOIN protocolo_paciente p ON p.codigo_protocolo = v.codigo
JOIN medicamento m ON m.nome = v.nome
ON CONFLICT (protocolo_id, medicamento_id) DO NOTHING;

-- TAX W: 1 ciclo com 12 aplicações semanais (D1 a D78).
INSERT INTO aplicacao (protocolo_id, numero, ciclo_numero, dia_ciclo, data_prevista, data_agendada,
                       dias_antes, dias_depois, status)
SELECT p.id, n, 1, 7 * n - 6, p.data_inicio + 7 * (n - 1), p.data_inicio + 7 * (n - 1), 0, 0, 'AGENDADA'
FROM protocolo_paciente p
CROSS JOIN LATERAL generate_series(1, 12) AS n
WHERE p.codigo_protocolo BETWEEN 'PTC2027-000001' AND 'PTC2027-000003'
ON CONFLICT DO NOTHING;

-- AC: 4 ciclos × D1, a cada 21 dias.
INSERT INTO aplicacao (protocolo_id, numero, ciclo_numero, dia_ciclo, data_prevista, data_agendada,
                       dias_antes, dias_depois, status)
SELECT p.id, n, n, 1, p.data_inicio + 21 * (n - 1), p.data_inicio + 21 * (n - 1), 0, 0, 'AGENDADA'
FROM protocolo_paciente p
CROSS JOIN LATERAL generate_series(1, 4) AS n
WHERE p.codigo_protocolo BETWEEN 'PTC2027-000004' AND 'PTC2027-000006'
ON CONFLICT DO NOTHING;

-- Prescrição só da primeira aplicação, emitida no próprio dia: uma linha por quimioterápico, com o
-- mesmo código. PTC2027-000006 foi avaliado como não apto: fica de fora da otimização.
INSERT INTO prescricao (codigo_prescricao, medicamento_id, dose_mg, status, data_prescricao, aplicacao_id)
SELECT 'PRE2027-' || right(p.codigo_protocolo, 6), pm.medicamento_id, pm.dose_mg,
       CASE WHEN p.codigo_protocolo = 'PTC2027-000006' THEN 'NAO_APTO' ELSE 'APTO' END, a.data_prevista, a.id
FROM aplicacao a
JOIN protocolo_paciente p ON p.id = a.protocolo_id
JOIN protocolo_medicamento pm ON pm.protocolo_id = p.id
WHERE p.codigo_protocolo BETWEEN 'PTC2027-000001' AND 'PTC2027-000006'
  AND a.numero = 1
ON CONFLICT DO NOTHING;

-- =====================================================================
-- Cenário da agenda no futuro (FICTÍCIO): cópia dos protocolos PTC2026-000001..010 deslocada 238 dias,
-- porque a agenda não sugere datas passadas. 26–27/10/2026 → 21–22/06/2027 (segunda e terça), depois
-- do fim dos TAX W de 2027. PTC2027-000110 cai sozinho em 21/06 com janela +1: a prévia deve sugerir
-- juntá-lo ao grupo de 22/06 com ganho de US$ 45, como no cenário original.
-- =====================================================================

INSERT INTO protocolo_paciente (codigo_protocolo, medicacao_subtipo, grupo, data_inicio, ciclos,
                                dias_intervalo, intervalo_minimo_dias)
SELECT 'PTC2027-0001' || right(p.codigo_protocolo, 2), p.medicacao_subtipo, p.grupo, p.data_inicio + 238,
       p.ciclos, p.dias_intervalo, p.intervalo_minimo_dias
FROM protocolo_paciente p
WHERE p.codigo_protocolo BETWEEN 'PTC2026-000001' AND 'PTC2026-000010'
ON CONFLICT (codigo_protocolo) DO NOTHING;

INSERT INTO protocolo_medicamento (protocolo_id, medicamento_id, dose_mg, via, dias_aplicacao)
SELECT novo.id, pm.medicamento_id, pm.dose_mg, pm.via, pm.dias_aplicacao
FROM protocolo_medicamento pm
JOIN protocolo_paciente velho ON velho.id = pm.protocolo_id
JOIN protocolo_paciente novo ON novo.codigo_protocolo = 'PTC2027-0001' || right(velho.codigo_protocolo, 2)
WHERE velho.codigo_protocolo BETWEEN 'PTC2026-000001' AND 'PTC2026-000010'
ON CONFLICT (protocolo_id, medicamento_id) DO NOTHING;

INSERT INTO aplicacao (protocolo_id, numero, ciclo_numero, dia_ciclo, data_prevista, data_agendada,
                       dias_antes, dias_depois, status)
SELECT p.id, n, n, 1, p.data_inicio + (n - 1) * p.dias_intervalo, p.data_inicio + (n - 1) * p.dias_intervalo,
       0, CASE WHEN p.codigo_protocolo = 'PTC2027-000110' THEN 1 ELSE 0 END, 'AGENDADA'
FROM protocolo_paciente p
CROSS JOIN LATERAL generate_series(1, p.ciclos) AS n
WHERE p.codigo_protocolo BETWEEN 'PTC2027-000101' AND 'PTC2027-000110'
ON CONFLICT DO NOTHING;

INSERT INTO prescricao (codigo_prescricao, medicamento_id, dose_mg, status, data_prescricao, aplicacao_id)
SELECT 'PRE2027-0002' || right(p.codigo_protocolo, 2), pm.medicamento_id, pm.dose_mg, 'APTO', a.data_prevista, a.id
FROM aplicacao a
JOIN protocolo_paciente p ON p.id = a.protocolo_id
JOIN protocolo_medicamento pm ON pm.protocolo_id = p.id
WHERE p.codigo_protocolo BETWEEN 'PTC2027-000101' AND 'PTC2027-000110'
  AND a.data_prevista IN (DATE '2027-06-21', DATE '2027-06-22')
ON CONFLICT DO NOTHING;
