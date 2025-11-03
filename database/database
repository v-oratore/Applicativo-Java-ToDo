--
-- PostgreSQL database dump
--

-- Dumped from database version 17.4
-- Dumped by pg_dump version 17.4

-- Started on 2025-11-03 16:10:19 CET

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 217 (class 1259 OID 1065660)
-- Name: bacheche; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.bacheche (
    id integer NOT NULL,
    utente_id integer NOT NULL,
    titolo_bacheca character varying(50) NOT NULL,
    descrizione text
);


ALTER TABLE public.bacheche OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 1065665)
-- Name: bacheche_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.bacheche_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.bacheche_id_seq OWNER TO postgres;

--
-- TOC entry 3757 (class 0 OID 0)
-- Dependencies: 218
-- Name: bacheche_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.bacheche_id_seq OWNED BY public.bacheche.id;


--
-- TOC entry 219 (class 1259 OID 1065666)
-- Name: todo_condivisioni; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.todo_condivisioni (
    todo_id integer NOT NULL,
    utente_id integer NOT NULL,
    bacheca_destinazione_id integer
);


ALTER TABLE public.todo_condivisioni OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 1065669)
-- Name: todos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.todos (
    id integer NOT NULL,
    bacheca_id integer,
    autore_id integer NOT NULL,
    titolo character varying(255) NOT NULL,
    descrizione text NOT NULL,
    url character varying(255),
    scadenza date,
    immagine bytea,
    posizione integer DEFAULT 0 NOT NULL,
    stato character varying(20) DEFAULT 'NON_COMPLETATO'::character varying NOT NULL,
    data_creazione date DEFAULT CURRENT_DATE NOT NULL,
    colore character varying(7) DEFAULT '#FFFFFF'::character varying
);


ALTER TABLE public.todos OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 1065678)
-- Name: todos_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.todos_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.todos_id_seq OWNER TO postgres;

--
-- TOC entry 3758 (class 0 OID 0)
-- Dependencies: 221
-- Name: todos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.todos_id_seq OWNED BY public.todos.id;


--
-- TOC entry 222 (class 1259 OID 1065679)
-- Name: utenti; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.utenti (
    id integer NOT NULL,
    username character varying(50) NOT NULL,
    password_hash character varying(255) NOT NULL
);


ALTER TABLE public.utenti OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 1065682)
-- Name: utenti_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.utenti_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.utenti_id_seq OWNER TO postgres;

--
-- TOC entry 3759 (class 0 OID 0)
-- Dependencies: 223
-- Name: utenti_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.utenti_id_seq OWNED BY public.utenti.id;


--
-- TOC entry 3571 (class 2604 OID 1065683)
-- Name: bacheche id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bacheche ALTER COLUMN id SET DEFAULT nextval('public.bacheche_id_seq'::regclass);


--
-- TOC entry 3572 (class 2604 OID 1065684)
-- Name: todos id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.todos ALTER COLUMN id SET DEFAULT nextval('public.todos_id_seq'::regclass);


--
-- TOC entry 3577 (class 2604 OID 1065685)
-- Name: utenti id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utenti ALTER COLUMN id SET DEFAULT nextval('public.utenti_id_seq'::regclass);


--
-- TOC entry 3745 (class 0 OID 1065660)
-- Dependencies: 217
-- Data for Name: bacheche; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.bacheche (id, utente_id, titolo_bacheca, descrizione) FROM stdin;
48	17	UNIVERSITA	Bacheca per le attività universitarie
49	18	UNIVERSITA	1
52	18	LAVORO	
\.


--
-- TOC entry 3747 (class 0 OID 1065666)
-- Dependencies: 219
-- Data for Name: todo_condivisioni; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.todo_condivisioni (todo_id, utente_id, bacheca_destinazione_id) FROM stdin;
\.


--
-- TOC entry 3748 (class 0 OID 1065669)
-- Dependencies: 220
-- Data for Name: todos; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.todos (id, bacheca_id, autore_id, titolo, descrizione, url, scadenza, immagine, posizione, stato, data_creazione, colore) FROM stdin;
77	49	18	1	1		2025-10-07	\\x	0	NON_COMPLETATO	2025-10-06	#FFFFFF
\.


--
-- TOC entry 3750 (class 0 OID 1065679)
-- Dependencies: 222
-- Data for Name: utenti; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.utenti (id, username, password_hash) FROM stdin;
17	admin2	jGl25bVBBBW96Qi9Te4V37Fnqchz/Eu4qB9vKrRIqRg=
18	admin	JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk=
\.


--
-- TOC entry 3760 (class 0 OID 0)
-- Dependencies: 218
-- Name: bacheche_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.bacheche_id_seq', 52, true);


--
-- TOC entry 3761 (class 0 OID 0)
-- Dependencies: 221
-- Name: todos_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.todos_id_seq', 77, true);


--
-- TOC entry 3762 (class 0 OID 0)
-- Dependencies: 223
-- Name: utenti_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.utenti_id_seq', 18, true);


--
-- TOC entry 3579 (class 2606 OID 1065687)
-- Name: bacheche bacheche_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bacheche
    ADD CONSTRAINT bacheche_pkey PRIMARY KEY (id);


--
-- TOC entry 3581 (class 2606 OID 1065689)
-- Name: bacheche bacheche_utente_id_titolo_bacheca_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bacheche
    ADD CONSTRAINT bacheche_utente_id_titolo_bacheca_key UNIQUE (utente_id, titolo_bacheca);


--
-- TOC entry 3586 (class 2606 OID 1065691)
-- Name: todo_condivisioni todo_condivisioni_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.todo_condivisioni
    ADD CONSTRAINT todo_condivisioni_pkey PRIMARY KEY (todo_id, utente_id);


--
-- TOC entry 3590 (class 2606 OID 1065693)
-- Name: todos todos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.todos
    ADD CONSTRAINT todos_pkey PRIMARY KEY (id);


--
-- TOC entry 3592 (class 2606 OID 1065695)
-- Name: utenti utenti_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utenti
    ADD CONSTRAINT utenti_pkey PRIMARY KEY (id);


--
-- TOC entry 3594 (class 2606 OID 1065697)
-- Name: utenti utenti_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utenti
    ADD CONSTRAINT utenti_username_key UNIQUE (username);


--
-- TOC entry 3582 (class 1259 OID 1065698)
-- Name: idx_bacheche_utente_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_bacheche_utente_id ON public.bacheche USING btree (utente_id);


--
-- TOC entry 3583 (class 1259 OID 1065699)
-- Name: idx_todo_condivisioni_todo_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_todo_condivisioni_todo_id ON public.todo_condivisioni USING btree (todo_id);


--
-- TOC entry 3584 (class 1259 OID 1065700)
-- Name: idx_todo_condivisioni_utente_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_todo_condivisioni_utente_id ON public.todo_condivisioni USING btree (utente_id);


--
-- TOC entry 3587 (class 1259 OID 1065701)
-- Name: idx_todos_autore_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_todos_autore_id ON public.todos USING btree (autore_id);


--
-- TOC entry 3588 (class 1259 OID 1065702)
-- Name: idx_todos_bacheca_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_todos_bacheca_id ON public.todos USING btree (bacheca_id);


--
-- TOC entry 3595 (class 2606 OID 1065703)
-- Name: bacheche bacheche_utente_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.bacheche
    ADD CONSTRAINT bacheche_utente_id_fkey FOREIGN KEY (utente_id) REFERENCES public.utenti(id) ON DELETE CASCADE;


--
-- TOC entry 3596 (class 2606 OID 1065708)
-- Name: todo_condivisioni todo_condivisioni_todo_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.todo_condivisioni
    ADD CONSTRAINT todo_condivisioni_todo_id_fkey FOREIGN KEY (todo_id) REFERENCES public.todos(id) ON DELETE CASCADE;


--
-- TOC entry 3597 (class 2606 OID 1065713)
-- Name: todo_condivisioni todo_condivisioni_utente_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.todo_condivisioni
    ADD CONSTRAINT todo_condivisioni_utente_id_fkey FOREIGN KEY (utente_id) REFERENCES public.utenti(id) ON DELETE CASCADE;


--
-- TOC entry 3598 (class 2606 OID 1065718)
-- Name: todos todos_autore_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.todos
    ADD CONSTRAINT todos_autore_id_fkey FOREIGN KEY (autore_id) REFERENCES public.utenti(id) ON DELETE CASCADE;


--
-- TOC entry 3599 (class 2606 OID 1065723)
-- Name: todos todos_bacheca_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.todos
    ADD CONSTRAINT todos_bacheca_id_fkey FOREIGN KEY (bacheca_id) REFERENCES public.bacheche(id) ON DELETE CASCADE;


-- Completed on 2025-11-03 16:10:19 CET

--
-- PostgreSQL database dump complete
--

