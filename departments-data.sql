--
-- PostgreSQL database dump
--

\restrict oXHEwWq1sf2rBPfSCNmKu3UIbxhSSdXXe34bx1gQYKZ2blXifCcTr2YQhbQFZ4E

-- Dumped from database version 17.7
-- Dumped by pg_dump version 17.7

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

--
-- Data for Name: departments; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.departments (id, name, slug, description, icon_url, display_order, is_active, created_at, updated_at) FROM stdin;
2	OB/GYN	ob-gyn	Obstetrics and gynaecology — pregnancy and women's reproductive health	\N	1	t	2026-04-29 19:43:26.215581+01	2026-06-05 12:21:48.817155+01
3	ENT (Ear, Nose & Throat)	ent-ear-nose-throat	Specialised ENT consultations and procedures	\N	10	t	2026-04-30 15:15:11.447438+01	2026-06-05 12:21:48.817155+01
1	General Surgery	general-surgery	Comprehensive surgical care	\N	19	t	2026-04-29 19:43:26.215581+01	2026-06-05 12:21:48.817155+01
4	Paediatrics	paediatrics	Medical care for infants, children and adolescents	\N	2	t	2026-06-05 12:21:48.817155+01	2026-06-05 12:21:48.817155+01
5	Family Medicine	family-medicine	Primary care for individuals and families	\N	3	t	2026-06-05 12:21:48.817155+01	2026-06-05 12:21:48.817155+01
6	Internal Medicine	internal-medicine	Diagnosis and treatment for adults	\N	4	t	2026-06-05 12:21:48.817155+01	2026-06-05 12:21:48.817155+01
7	Cardiology	cardiology	Heart and cardiovascular care	\N	5	t	2026-06-05 12:21:48.817155+01	2026-06-05 12:21:48.817155+01
8	Nephrology	nephrology	Kidney health and disease management	\N	6	t	2026-06-05 12:21:48.817155+01	2026-06-05 12:21:48.817155+01
9	Urology	urology	Urinary tract and male reproductive health	\N	7	t	2026-06-05 12:21:48.817155+01	2026-06-05 12:21:48.817155+01
10	Orthopaedic Surgery	orthopaedic-surgery	Bone, joint and musculoskeletal surgery	\N	8	t	2026-06-05 12:21:48.817155+01	2026-06-05 12:21:48.817155+01
11	Neurology	neurology	Disorders of the brain, spine and nervous system	\N	9	t	2026-06-05 12:21:48.817155+01	2026-06-05 12:21:48.817155+01
12	Endocrinology	endocrinology	Hormonal and metabolic conditions	\N	11	t	2026-06-05 12:21:48.817155+01	2026-06-05 12:21:48.817155+01
13	Gastroenterology	gastroenterology	Digestive system care	\N	12	t	2026-06-05 12:21:48.817155+01	2026-06-05 12:21:48.817155+01
14	Dermatology	dermatology	Skin, hair and nail conditions	\N	13	t	2026-06-05 12:21:48.817155+01	2026-06-05 12:21:48.817155+01
15	Physiotherapy	physiotherapy	Movement and rehabilitation therapy	\N	14	t	2026-06-05 12:21:48.817155+01	2026-06-05 12:21:48.817155+01
16	Hematology	hematology	Blood disorders and diagnostics	\N	15	t	2026-06-05 12:21:48.817155+01	2026-06-05 12:21:48.817155+01
17	Mental Health	mental-health	Counselling and emotional wellbeing	\N	16	t	2026-06-05 12:21:48.817155+01	2026-06-05 12:21:48.817155+01
18	Psychiatry	psychiatry	Diagnosis and treatment of mental health conditions	\N	17	t	2026-06-05 12:21:48.817155+01	2026-06-05 12:21:48.817155+01
19	Dietician	dietician	Nutrition and dietary guidance	\N	18	t	2026-06-05 12:21:48.817155+01	2026-06-05 12:21:48.817155+01
\.


--
-- Name: departments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.departments_id_seq', 19, true);


--
-- PostgreSQL database dump complete
--

\unrestrict oXHEwWq1sf2rBPfSCNmKu3UIbxhSSdXXe34bx1gQYKZ2blXifCcTr2YQhbQFZ4E

