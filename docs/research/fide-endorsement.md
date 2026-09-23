# FIDE endorsement process and tooling

Research ticket: `.wayfinder/fide-swiss-engine/05-fide-endorsement.md`. Researched 2026-09-23.

## TL;DR

- **C.04.A is gone.** The old appendix "C.04.A Endorsement of a software program" was merged into **Handbook C.02.03, Article 7 "Tournament Handler Programs"**, in force since **1 March 2026** [S1][S2]. The C.04.A handbook page is now labelled "effective till 31 December 2024" [S3]. The SPP Commission is now the **SPP department of the Technical Commission (TEC)** [S2 p.10][S4].
- The new route is the **Technical Acceptance Process**: register as vendor, VCL self-assessment, SDPC, **TAPC** (technical acceptance, by TEC testers, approved by FIDE Council), then optional **FEAP** ("FIDE Endorsed"), which **requires a commercial agreement with FIDE** [S1 §2.9, §7.7-7.8][S2 p.22].
- **What is tested:** pairings **and now tie-breaks**. The program's **PTC** (Pairings and Tie-Breaks Checker, a CLI) is fed **50,000** tournaments made by an accepted program's **RTG**. At most 10 discrepancies are allowed, and each one is analysed. It also runs the other way: the candidate's own RTG makes 50,000 tournaments for existing PTCs [S5 §3.9.4]. The old C.04.A used 5,000 tournaments [S3a].
- **Fees (TEC Manual 1.24):** USD 300 initiation plus USD 900 THP classification, so **USD 1,200** for a THP TAPC. Preliminary testing costs another USD 300. Fees are not refunded if you fail [S5 Annex A][S1 §5.8].
- **Endorsed programs: Dutch only.** The C.02.04 register lists 11 THPs, all for the Dutch system. Nine use JaVaFo, one uses bbpPairings and one has its own engine. **All show TAPC expiry 2026/02/01** [S6]. **No program is currently listed for Dubov, Burstein, Lim, Swiss Team or Double-Swiss.**
- **Tooling:** JaVaFo (Ricca) and bbpPairings are the historic reference engine, checker and RTG. **Gacrux** (Otto Milvang, MIT, the "flagship" reference of TEC) now provides a pairing checker, tie-break checker, tournament generator and a public corpus of 120,000 test tournaments [S2 p.14, p.41, p.60][S9][S10].
- **New TAPCs are on hold.** They will start under the *incoming* Commission, once the THP VCL is final and a new Acceptance Cycle is announced [S2 p.31, p.35, p.50].

## Sources

Primary sources only. Handbook pages were fetched on 2026-09-23. Where spp.fide.com was down for maintenance, I used Wayback snapshots of it.

| Id | Source |
|---|---|
| S1 | FIDE Handbook C.02.01 *General Regulations* (Council 11/12/2025, applied 01/03/2026). https://handbook.fide.com/chapter/GeneralRulesAndRegulations032026 |
| S1b | FIDE Handbook C.02.03 *Chess Equipment with Electronic Components*, §7 THPs (Council 11/12/2025, applied 01/03/2026). https://handbook.fide.com/chapter/ChessEquipmentWithElectronicComponenets032026 |
| S2 | TEC, *2026 FIDE Congress – Commission Meeting and Term Report 2022–2026* (Samarkand, 20 Sep 2026). http://tec.fide.com/wp-content/uploads/2026/09/TEC-2026-Congress-Meeting.pdf |
| S3 | Handbook C.04.A page (now "effective till 31 December 2024", the body no longer renders). https://handbook.fide.com/chapter/C04A |
| S3a | Old C.04.A full text. https://old.fide.com/component/handbook/?id=206&view=article . spp.fide.com copy (adds the error-fix deadlines): https://web.archive.org/web/20260215045754/http://spp.fide.com/c-04-a-appendix-endorsement-of-a-software-program/ |
| S3b | FE-1 application form (C.04 Annex-1). https://www.fide.com/FIDE/handbook/C04Annex1_FE1.pdf |
| S3c | VCL19 Verification check-list (C.04 Annex-4). http://spp.fide.com/wp-content/uploads/2020/04/C04Annex4_VCL19.pdf |
| S4 | tec.fide.com pages: /about/, /spp/, /what-we-do/, /endorsement/. https://tec.fide.com/endorsement/ |
| S5 | *FIDE TEC Manual* v1.24, 07 Mar 2026. https://tec.fide.com/wp-content/uploads/2026/03/FIDE-TEC-Manual-01.24-1.pdf (linked from https://tec.fide.com/fide-technical-manual/) |
| S6 | FIDE Handbook C.02.04 *FIDE Certified and Endorsed Equipment* (register). https://handbook.fide.com/chapter/FIDECertifiedAndEndorsedEquipment032026 |
| S6a | FEP22 endorsed programs list (older annex). https://handbook.fide.com/files/handbook/C04Annex3_FEP22.pdf |
| S6b | Old spp.fide.com "Endorsed Tournament Managers" page (Wayback 2026-05-15). https://web.archive.org/web/20260515112844/http://spp.fide.com/endorsed-tournament-managers/ |
| S7 | Endorsement reports by R. Ricca: Vega 2017 http://tec.fide.com/wp-content/uploads/2024/05/VegaReport.pdf ; Chess Online (COPP) 2023 http://tec.fide.com/wp-content/uploads/2024/05/COPP-Report.pdf ; the others (SwissSys, Swiss Manager, Swiss Master, Swiss-Chess) are linked from S4 /endorsement/ |
| S7a | TEC news "FIDE Endorses New Chess Online Pairing Program" (2024-05-24). https://tec.fide.com/2024/05/24/fide-endorses-new-chess-online-pairing-programme/ |
| S7b | TEC Annual Report 2024. https://tec.fide.com/wp-content/uploads/2024/10/FIDE-Technical-Comission-2024-Annual-Report.pdf |
| S8 | JaVaFo Advanced User Manual (Rel. 2.2, doc dated 2018). http://www.rrweb.org/javafo/aum/JaVaFo2_AUM.htm (the live site returned HTTP 999, so I read the Wayback 2026-08-20 copy) |
| S8a | bbpPairings README (master, repo pushed 2026-07-31, latest release v6.0.0 of 2026-02-01). https://github.com/BieremaBoyzProgramming/bbpPairings/blob/master/README.txt |
| S9 | Gacrux software manual v1.7/1.8 (O. Milvang, 2026). http://gacrux.no/spp/doc/GacruxSoftware.pdf ; portal https://www.gacrux.no/ ; test corpus https://www.gacrux.no/spp/tournaments/tournaments.zip (444 MB, last modified 2026-04-21) |
| S10 | Gacrux source: https://github.com/OttoMilvang/TieBreakServer (README, changelog, MIT "Copyright (c) 2024 FIDE") |
| S11 | C.02.03 annexes: TRF26 https://handbook.fide.com/files/handbook/TRF26.pdf , MTB26 https://handbook.fide.com/files/handbook/MTB26.pdf , ETT26 https://handbook.fide.com/files/handbook/ETT26.pdf |
| S12 | Handbook C.04.2 *General handling rules* (from 1 Feb 2026), Art. 1.4. https://handbook.fide.com/chapter/GeneralHandlingRulesForSwissTournaments202602 |

## 1. Governance and where the rules live now

- The SPP Commission was folded into TEC. "References to the SPPC in the Handbook now read as TEC." [S2 p.10] The COPP report says the same thing: "All references to … SPPC … should be read as referring to the Technical Commission" [S7].
- "C.04.A software endorsement was merged into C.02.03; TRF16 became TRF26" [S2 p.10]. The new structure is [S2 p.21]:
  - C.02.01: common rules, definitions and the acceptance process.
  - C.02.03: "absorbs the former C.04.A".
  - C.02.04: public register.
  - The TEC Manual carries the operational detail (forms, VCLs, fees, older TRF formats).
- Where the Handbook and the TEC Manual conflict, the **Handbook prevails** [S5 §1.1.2][S2 p.24].
- Applications are **online only** (Cognito forms at https://www.cognitoforms.com/FIDE/TECMenu). Vendors must register first [S1 §5.1, §5.3][S5 ch.2].
- C.04.2 (2026) Art. 1.4 still binds pairing systems to software. Different FIDE-approved programs "must be able to arrive at identical pairings". A system's use "is deprecated, unless a tournament handler program approved by FIDE is available for them, provided with a free pairing-checker able to verify tournaments run with that system" [S12].

## 2. The process (current, C.02 + TEC Manual)

### 2.1 Stages and concepts

The six stages are Registration → Acceptance Cycle → VCL self-assessment → SDPC → TAPC → FIDE Endorsement [S2 p.22]. The three concepts are distinct [S1 §2][S2 p.22]:

- **SDPC** is the vendor's self-declaration. For THPs it is a prerequisite only. C.02.01 §6.2 says an SDPC by itself "can only be obtained for chess equipment without electronic components". Software counts as equipment *with* electronic components [S1 §7.3.6].
- **TAPC** is technical acceptance by TEC. A TAPC needs all of these [S1 §7.1]:
  - the SDPC has been submitted and the fees paid
  - TEC has done its testing
  - an English datasheet and an English user manual exist
  - **FIDE Council (or GA) approves** the recommendation of TEC.

  At least **three testers** are appointed by the TEC Chair [S1 §7.3.1]. The testers evaluate with the same VCL the vendor used [S1 §7.3.7].
- **FEAP / "FIDE Endorsed"** is possible only with a valid TAPC. It needs "a commercial agreement with FIDE, approved by the FIDE Council" [S1 §2.9, §7.7-7.8][S5 §3.3.3]. The terms of that agreement are not published. On 1 March 2026, every product without an endorsement agreement was switched to plain TAPC status for its current version [S1 §7.10].
- Usage by tournament level [S1 §8]:
  - **Level 1** events *require* endorsed products, unless FIDE Council waives this. If no endorsed product exists, an accepted one may be used.
  - **Level 2** events recommend endorsed products and tolerate accepted ones.
  - **Level 3** events recommend endorsed or accepted products and tolerate self-declared ones.

### 2.2 THP conditions of compliance (C.02.03 §7.1)

A THP must [S1b §7.1.1-7.1.3]:

- (a) import FIDE rating lists
- (b) pair with at least one FIDE Swiss system **and support all FIDE-defined acceleration methods**
- (c) produce final standings with **all tie-breaks listed in MTB26**
- declare compliance per pairing system
- run in a "FIDE mode" that provides:
  - an English UI plus a full manual or online help
  - TRF import/export in the latest format, with backward compatibility
  - a **free PTC and RTG** ("unless exempted by TEC")
  - a controlled test environment (for example a VM for web apps; COPP delivered one [S7]).

Extra FIDE-mode features must "not compromise the integrity" of the declared systems [S1b §7.1.4]. Software must use semantic versioning with at least MAJOR.MINOR [S1 §2.17, §5.9]. A **major version bump automatically revokes the TAPC** [S1 §5.11.1][S5 §3.5].

### 2.3 How a pairing/tie-break engine is verified (TEC Manual §3.9)

- **First TAPC for a given pairing system:** TEC appoints a **4-person subcommittee**, which reports "within nine months" [S5 §3.9.2]. The old rule was: named at one Congress, report at the next [S3a A.6].
- **A system that already has an accepted THP** is verified automatically, using that THP's PTC and RTG [S5 §3.9.3-3.9.4]:
  1. An external RTG generates **50,000** random tournaments. The candidate PTC processes them and logs discrepancies. If there are ≤10, each one is analysed. If there are >10, the revocation rules apply [S5 §3.9.4.4].
  2. Each discrepancy is classified as (a) an RTG/input error, which goes to the RTG provider or, if the accepted THP is at fault, triggers manual revocation of *that* THP; (b) a candidate error, which the candidate must fix; or (c) an interpretation divergence, which goes to TEC for an official clarification and a later rule fix [S5 §3.9.4.5-8].
  3. If the candidate has its own RTG, it generates 50,000 tournaments for "one or more available PTCs" [S5 §3.9.4.9].
- TEC "expects this step to be a routine formality. No TAPC application should be submitted unless the pairing system fully complies" [S5 §3.9.4.3 note].
- **External-engine exemption:** a THP that uses an external engine already integrated into another accepted THP can be exempted, but TEC must have access to the engine's input files, and preferably its output files [S5 §3.9.4.1]. This is how the JaVaFo- and bbpPairings-based THPs work.
- The VCL is judged as it stood when the TAPC application was submitted [S5 §3.4.4].
- In practice the evaluation is a VCL walk-through. The reports go item by item through VCL.01-19: FIDE mode as the default, pairing by TPN, no TPN changes after round 4, Baku acceleration, TRF16 import/export incl. non-standard scoring, result codes, forfeits only 1F-0F/0F-1F/0F-0F, PAB value configurable, HPB/FPB warnings, the rating list, tie-breaks [S3c][S7]. VCL.19 (tie-breaks) was "ignored for now (will be evaluated in 2025)" for COPP [S7]. The current THP VCL is **not yet published**. VCLs were moved online and "could not be completed within the current Commission's term" [S2 p.28, p.31].

### 2.4 Timelines and cycles

- **NRD (New Rules Date):** successive NRDs are ≥4 years apart, and new rules are published ≥12 months before the NRD [S1b §7.3.1-7.3.2]. The current NRD is 1 Feb 2026 (C.04 in force), which matches the TAPC expiry column in S6.
- **Acceptance Cycle:** lasts ≥3 years. It starts 6 months before the NRD and runs until new rules are published. TAPCs are valid until the next cycle starts [S1b §7.3.3-7.3.4]. A THP accepted before the NRD may be used immediately [S1b §7.3.5].
- **Transition Period:** ≥1 year from the start of a new cycle. THPs whose TAPC lapsed may keep being used, with their old rules, during the TP, or beyond it if re-acceptance has started [S1b §7.3.6-7.3.7].
- **Complaints:** after a credible complaint, TEC sets a fix period of ≤3 months [S1b §7.3.4.1]. The old rule was 2 weeks for major errors and 2 months for minor ones [S3a, spp.fide.com version].
- **After a rejection:** no new TAPC application for **1 year** [S1 §5.12].
- **Status today:** new TAPC assessments will "commence under the newly appointed Commission". The remaining steps are: publish the final Manual and VCL, announce the Acceptance Cycle and the vendor implementation period, then SDPC, then TAPC [S2 p.31-35]. Pipeline: 44 tracked records, 30 of them THP-only. Diclano is at TAPC. UTU Swiss and STOP are "Not-Operational" [S2 p.34].
- The **old C.04.A timelines** are superseded and kept here for history [S3a]:
  - 4-year endorsement cycles (Jan YearX+1 to Dec YearX+4, X a leap year), with no endorsements in the last year
  - requests due ≥4 months before a Congress
  - interim certificates during the Transition Period.

### 2.5 Fees (TEC Manual Annex A)

| Item | USD |
|---|---|
| Mandatory initiation fee (once off) | 300 |
| Equipment *with* electronic components (THP): preliminary testing / classification | 300 / 900 |
| Example: TAPC for THP | 300 + 900 = **1,200** |
| Example: THP with preliminary testing | 300 + 300 + 900 = **1,500** |
| Example: THP + Hybrid Tournament Management | 2,100 |

Source: [S5 Annex A]. Fees are payable whether or not a TAPC or FEAP is issued [S1 §5.8.4]. The vendor also ships a functional sample of the product to the FIDE Office for archiving [S1 §7.3.9]. Fee levels were still "to confirm" at the 2026 meeting [S2 p.58]. The old C.04.A named no fee.

## 3. Required interface of the submitted program

### 3.1 PTC (checker mode)

TEC Manual §3.9.4.2.c [S5]: the PTC is "embedded in the THP", contains the pairing engine and the tie-break routines, and has "a CLI that can be used freely by anyone (without the user interface)". The canonical example is:

```
yourprogram.exe -check FIDE_Report_File.fid
```

- It **must** read TRF26. It **should** also read TRF16 and TRF06.
- For each round it must rebuild the tournament, pair the round with the embedded engine, **check the standings for the listed tie-breaks**, and report which pairings and standings are *not* consistent.
- The flag name `-check` is only an example ("like in"). The existing tools use different flags (see 3.3).
- The old C.04.A wording was the same, but covered pairings only and required TRF16 [S3a A.4].

### 3.2 RTG

[S5 §3.9.4.2.d][S1b §7.2.4]:

- a freely available tool, "preferably run from a CLI"
- "widely parametrized (for number of players, number of teams, number of rounds, unplayed games, acceleration methods, tie-breaks used)"
- produces one full **TRF26** file per tournament
- must strictly follow the pairing rules and compute standings correctly
- results should roughly follow the FIDE rating probability table (see the TEC *Statistical Model for Chess Tournament Simulations* on https://tec.fide.com/official-documents/).

### 3.3 Existing de-facto CLIs (what TEC actually runs)

| Tool | Checker | RTG | Pair next round | Systems |
|---|---|---|---|---|
| JaVaFo 2.2 [S8] | `javafo in.trf -c [round]` | `javafo [model.trf] -g [cfg\|seed] [-b] -o out.trf` | `javafo in.trf [-b] -p out.txt [-l checklist]` | Dutch |
| bbpPairings [S8a] | `bbpPairings (--dutch\|--burstein) in.trf -c [-l]` | `... (model -g \| -g [cfg]) -o out.trf [-s seed]` | `... in.trf -p [out]` | Dutch 2025/26 rules; Burstein "flawed … previous version … not endorsed" |
| Swiss-Chess [S6b] | `WinSwiss_Console.exe tournament_TRF` | `WinSwiss_Console.exe <dir>\<pfx>xxxx.<ext> /G NNNN MMMM` | — | Dutch |
| Gacrux [S9][S10] | `python pairingchecker.py -i in.trf -c [-n r] [-dT\|-d@]` | `python tournamentgenerator.py -g N -p P -n R [-a] -o T%d.trf` | `pairingchecker.py -i in -o out -p` | `dutch` (manual v1.8); README 2026-09 also lists `berger`, `fideteam`, `fideteam-typeb` |

- **JaVaFo pairing output format** [S8]: line 1 is the number of pairs P. Then P lines follow, each holding `white black` pairing IDs. The PAB is written as `id 0`.
- **JaVaFo RTG defaults** [S8]: 15-415 players and 5-17 rounds. Game results follow Milvang's formula. Configurable rates cover forfeits, ZPB, HPB, FPB and the scoring points. A seed makes a run reproducible, and the seed is echoed in TRF field 012. The old 5,000-tournament loop was `for /L %p IN (1000,1,5999) do javafo -g -o test%p.trf`.
- **bbpPairings** exit codes: 0 ok, 1 no valid pairing, 2 unexpected error, 3 invalid request, 4 size limit, 5 file error. It outputs TRF26 codes [S8a].
- **Gacrux** status codes: 0 means OK/check true, 1 means OK/check false, 2 means no legal pairing, 4xx/5xx are errors. Output is JSON or text. It reads TRF-26 and JCH [S9].

### 3.4 Data format

- TRF26 is Annexure A of C.02.03. Record 192 carries the tournament type code (ETT26). Records 202/212 carry the tie-break list (MTB26) [S11].
- ETT26 codes include:
  - `FIDE_DUTCH_2017` and `FIDE_DUTCH_2026`
  - `FIDE_DUBOV` and `FIDE_BURSTEIN`
  - `*_BAKU` variants
  - `FIDE_DOUBLESWISS` ("possible evolution")
  - a family of `FIDE_TEAM_TYPEA|TYPEB|…_MP|GP[_…]` codes.
- **There is no Lim code in ETT26** [S11].

## 4. Endorsed / accepted programs per system

The C.02.04 register, THP section [S6]. Every row has TAPC ☑, Endorsed ☑ and expiry **2026/02/01**:

| Program | Version | Author | System | Engine | Decision |
|---|---|---|---|---|---|
| Vega | 7.6.0 | L. Forlano (ITA) | Dutch | JaVaFo | Göynük, Oct 2017 |
| SwissSys | 9.6 | T. Suits (USA) | Dutch | bbpPairings | Minsk, Apr 2018 |
| SwissMaster | 5.7 | KNSB (NED) | Dutch | JaVaFo | Minsk, Apr 2018 |
| Swiss-Manager | 13 | H. Herzog (AUT) | Dutch | JaVaFo | Minsk, Apr 2018 |
| Swiss-Chess | 9.05 | F.-J. Weber (GER) | Dutch | Internal | Minsk, Apr 2018 |
| UTU Swiss | — | N. Hayward (UK) | Dutch | JaVaFo | Abu Dhabi, Feb 2020 |
| ChessManager | — | T. Żyźniewski (POL) | Dutch | JaVaFo | Abu Dhabi, Feb 2020 |
| STOP | — | A. Lenhard (GER) | Dutch | JaVaFo | Abu Dhabi, Feb 2020 |
| TournamentService | — | H. Heggelund (NOR) | Dutch | JaVaFo | Chennai, Aug 2022 |
| Tornelo | — | D. Cordover (AUS) | Dutch | JaVaFo | Chennai, Aug 2022 |
| Chess Online (COPP) | 7.7 | A. Goryachev (RUS) | Dutch | JaVaFo | Toronto, Apr 2024 (CM1-2024/19 [S7a]) |

Per system:

- **Dutch (C.04.3):** the 11 programs above. Only Swiss-Chess has an internal engine. The reference engines are JaVaFo and bbpPairings, but the engines themselves are not listed.
- **Dubov (C.04.4.1):** none in the current register. Vega was endorsed for Dubov at Turin 2006 (v4.2, "FPC availability NO") [S6a][S6b], but that row is absent from C.02.04 [S6].
- **Burstein (C.04.4.2):** none. Vega was "requesting" Burstein endorsement in 2017 [S7]. The bbpPairings Burstein implementation is "not endorsed" [S8a].
- **Lim (C.04.4.3):** none.
- **Swiss Team (C.04.6):** none. Gacrux has a `fideteam` method [S10], but Gacrux "is not itself FIDE approved" [S2 p.41].
- **Double-Swiss (C.04.5):** none.
- **Acceleration (C.04.7):** not endorsed separately. Supporting all FIDE-defined methods is a THP condition [S1b §7.1.1.b].

Under C.04.2 Art. 1.4, every system except Dutch is therefore currently "deprecated" in the technical sense [S12]. A first acceptance for any of them follows the subcommittee path [S5 §3.9.2].

## 5. Tooling and test corpora

- **JaVaFo** (Roberto Ricca) is the pairing engine, checker and RTG for the Dutch system. Most endorsed THPs rely on it for FPC/RTG availability [S6b][S8]. The public AUM is Rel. 2.2 from 2018. Whether a release implementing the 2026 Dutch rules exists was **not confirmed** from primary sources.
- **bbpPairings:** "implements the 2025 rules for the Dutch system (the effective date … delayed to 2026)" and reads and writes TRF-2026. Release v6.0.0 came out on 2026-02-01 [S8a]. TEC reports cite it as the stronger comparison engine. The COPP report notes that JaVaFo "has some very minor weaknesses … when compared to" bbpPairings [S7].
- **Gacrux** (Otto Milvang, © 2024 FIDE, MIT) [S2 p.14, p.41-42, p.60-62][S9][S10]:
  - It has a pairing checker, tie-break checker, tournament generator and web server, plus a JSON data model (JCH).
  - It was "tested on 120,000+ generated tournaments", and that corpus is published as `tournaments.zip` (444 MB).
  - It is positioned as the THP benchmark for TAPC. TEC is still deciding whether a Gacrux comparison will be *required*, which test sets to publish, and who rules on disagreements. The owners are Milvang and Ricca, with no date set [S2 p.62, p.64].
- **FIDE-published test corpora:** no official FIDE test suite exists. The "corpus" is RTG-generated on demand (50,000 tournaments), plus the Gacrux 120k set, whose publication status is under discussion [S2 p.62].
- The probability model for RTG results is the TEC paper *Statistical Model for Chess Tournament Simulations* (Milvang) [S4 official-documents].

## 6. Ambiguities and inconsistencies (flagged)

1. **5,000 vs 50,000 tournaments.** The old C.04.A and the FE-1 form used 5,000 and "1 difference every 500" [S3a][S3b]. TEC Manual 1.24 uses 50,000 with the same "≤10 discrepancies" cap [S5]. Taken literally, that tightens the tolerance from 1/500 to 1/5,000.
2. **>10 discrepancies means "revocation applies"** [S5 §3.9.4.4.d]. This is odd wording for a candidate that has no TAPC yet. It probably means rejection.
3. **"CC" is undefined** in C.02.03 §7.3.4/7.3.6-7 (probably a leftover "Certification Cycle"). The Manual glosses "CC - Acceptance Cycle" [S5 §1.3], while C.02.01 uses "AC" [S1 §3.1].
4. **The fee table columns are unlabeled.** Reading "300 / 900" as preliminary testing vs classification is inferred from the worked examples [S5 Annex A]. The fees were not final as of 2026-09-20 [S2 p.58].
5. **All THP TAPCs show expiry 2026/02/01**, yet the new Acceptance Cycle has not opened [S2 p.35][S6]. So it is unclear whether *any* THP currently holds a valid TAPC for the 2026 rules. Legacy use continues under the TP clause [S1b §7.3.7].
6. **Circular bootstrap for the non-Dutch systems.** Automated verification presumes an accepted THP with a PTC and RTG for the same system [S5 §3.9.4.1]. No such THP exists for Dubov, Burstein, Lim, Team or Double-Swiss.
7. **Vega's Dubov endorsement disappeared** between FEP22 and C.02.04. I found no stated decision.
8. **The Lim system has no ETT26 code**, so a TRF26 cannot declare a Lim tournament [S11].
9. **Gacrux method support is inconsistent.** The manual v1.8 says "only dutch". The README (Sep 2026) lists `berger`, `fideteam` and `fideteam-typeb` [S9][S10].
10. **Engines vs programs.** The register lists THPs, not engines. A THP "must" import rating lists, have an English UI and manual, etc. [S1b §7.1]. A pure library cannot obtain a TAPC by itself. It gets recognised through a THP that embeds it, via the external-engine exemption [S5 §3.9.4.1], as JaVaFo and bbpPairings do.
11. **Old error-fix deadlines** (2 weeks major, 2 months minor) appear only in the spp.fide.com copy of C.04.A, not in the handbook copy [S3a]. They are now superseded by "≤3 months" [S1b §7.3.4.1].

## Implications for the library

- **Target the PTC/RTG contract, not "endorsement".** The library cannot itself be a TAPC'd THP. What TEC actually tests is the engine through a **checker CLI** and an **RTG CLI** over TRF26. The TRF CLI wrapper should therefore offer:
  - `check <trf> [round]`: re-pair every round and diff the pairings, and **also verify MTB26 tie-breaks/standings**
  - `pair <trf>`: output the next round, ideally in the JaVaFo pairs format too
  - `generate`: an RTG with seed, player/round/team counts, bye/forfeit rates, acceleration and tie-break list, writing TRF26
  - JaVaFo/bbpPairings-compatible flags (`-c`, `-p`, `-g`, `-o`, `-s`) and distinct exit codes, so that existing THPs and TEC scripts can drop it in.
- **Tie-breaks are in scope for acceptance.** The PTC must check standings [S5], and a THP must implement all MTB26 tie-breaks [S1b §7.1.1.c]. The C.07 module is therefore part of endorsement-grade conformance, not an extra.
- **The Dutch conformance bar is ≤10 discrepancies per 50,000 RTG tournaments** against an accepted PTC/RTG, run in both directions. Build a harness that runs:
  - our checker over bbpPairings/JaVaFo/Gacrux RTG output
  - their checkers over our RTG output
  - the Gacrux 120k corpus as a regression suite.

  Make this a CI gate.
- **For the non-Dutch systems the library could be the first reference.** There is no accepted program for Dubov, Burstein, Lim, Swiss Team or Double-Swiss. The first TAPC for each goes through a 4-person subcommittee (≤9 months). A free, open, CLI-driven checker and RTG for these systems is exactly what C.04.2 Art. 1.4 says is missing. Engage with TEC (Ricca/Milvang) early and cross-check against Gacrux `fideteam`.
- **Support all FIDE acceleration methods** (C.04.7), because THPs must [S1b §7.1.1.b]. RTG output should be able to exercise them.
- **Use strict semantic versioning.** FIDE ties TAPC validity to MAJOR versions [S1 §5.11.1]. A THP embedding the library would lose its TAPC on a major bump of the embedded engine, so keep the pairing-behaviour contract stable within a major version.
- **Accept TRF26 as the primary format; read TRF16 (and ideally TRF06).** Handle record 192 ETT26 codes, and define a documented extension for Lim until FIDE adds a code.
- **Costs and commerce do not bind the library directly.** The USD 1,200 TAPC fee and the commercial FEAP agreement fall on whichever THP vendor embeds the library. Endorsement is a vendor/commercial status, while conformance is the technical goal.

## Open questions

1. When will the new Acceptance Cycle open, and when will the final THP VCL be published (it replaces VCL19)? What target durations per stage will the TEC Manual set [S2 p.58]?
2. Will a Gacrux comparison become mandatory for TAPC? Which Gacrux test sets will be officially published [S2 p.62]?
3. Is the 50,000-tournament / ≤10-discrepancy rule intended (1/5,000), or a typo for 5,000?
4. How does TEC bootstrap acceptance for systems with no accepted PTC/RTG (Dubov, Burstein, Lim, Team, Double-Swiss)? Is a subcommittee the only path, and could an open-source engine plus a thin THP be the first one?
5. Is there a JaVaFo release implementing the 2026 Dutch rules? Which engine (bbpPairings v6, Gacrux, JaVaFo) is TEC's de-facto oracle for 2026 Dutch?
6. Can an engine or library (as opposed to a THP) be registered in its own right, for example as a reference engine that THPs then claim under the external-engine exemption?
7. Why is there no Lim code in ETT26, and is Lim still expected to have tool support?
8. What is the status of Vega's Dubov endorsement, and of the "Not-Operational" UTU Swiss and STOP entries, in the register?
9. What does the FEAP commercial agreement contain (fees, royalties, branding)? It is not public.
