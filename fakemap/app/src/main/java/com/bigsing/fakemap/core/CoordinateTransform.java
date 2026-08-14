package com.bigsing.fakemap.core;

/** WGS-84 and GCJ-02 conversion used only at the Amap display boundary. */
public final class CoordinateTransform {
    private static final double AXIS = 6378245.0;
    private static final double ECCENTRICITY = 0.00669342162296594323;
    private static final int FIRST_SMALL_ISLAND_INDEX = 2;
    private static final double SMALL_ISLAND_TOLERANCE_DEGREES = 0.015;
    /**
     * Natural Earth 1:10m Admin-0 geometry (public domain), upstream commit
     * ca96624a56bd078437bca8184e78163e5039ad19. All China components are retained,
     * topology-preserving simplified and delta encoded; Hong Kong, Macao and Taiwan
     * features are explicit exclusions from this mainland GCJ-02 engineering mask.
     */
    private static final double[][][] CHINA_MASK = decodePolylines("cwwjEqrt`Nwo_@~rYy|p@yc@gic@j|KmqOew@{|Y_ao@a~P?yli@pz{@_eRl~fAqy|B|qx@_oj@klGnyJ`n^cqKjdOhqCzjpAa}Il`SswI|nfAuy_@tfbAip\\|pE~}LxdYtyCxhm@gq`@lzMotOfbd@gr_@ajUi~Lz{Gck^mo@eqb@pwWcjT|`f@laEtpi@_n`@dpImvE`rKowKfth@hpCzea@pmKxgIkbTtqNwtFflX`lErmVsnSre^a|FbxA_pXe~cA`kOodMkjBieRqb`@yat@_xe@ldt@sz_@_iMkiKbmNkid@pb@utJtoPa`h@vyBgdP{bLmxMnNatKpvM{u[ffrAoNvdh@xzL`yOd|Lhs@}qJboh@}|v@nnYohSgk[c_l@fsi@kan@_zC{iDmdg@ixSugOs`XvnD{wMhsMuhq@i{]mgKchd@bnAum_@wkm@ysgA~^ip]anQ`rMcwPwjHbqK{r_@xd@}kd@sld@infAjtb@g|SzoK|qD`mQ}nG{k@g~i@wyP_g\\|hCinRugMacQtbRmpLacQuy^am^u}Xkw_@ioFq`OkkUkaRcZikLmkQw`Egt[riKuy_B{~N_~~@rqCosn@abl@srt@q|Sg`AosRusq@wrIesFa`p@g{xBnbAgeT_|SetdAazVgsQcfE{yjAu{Hg}CgpSv_HymH_jJmmnA~iWon`@alSedKcty@umFnb@wlJx_`@_lInb@itSo{o@jnEuaUimH}aKspW~wGc~HdsSa|XusLkl`Cl_hAmkTmuEyjj@ljG_g\\ilI}wHjmDitMusY_ePbjo@njCflp@e{Ij`WwrIf`A}aXg{n@}sA{~v@glJqxImuFqdp@djBsr]ggj@an}CpzFsoRp{T}pDv~H_q]y`P{sx@|bTiox@skWsrMmt_@{kBo|UboaAe~fAcqe@qqx@wnNoupCwnpAemr@w_Fgb@qdXzkm@umrCy|@uasCrzKkePbdS|iAs~Euma@{hd@go{@c_A_ht@gxWsjJqlDwaSijLywBakT|t@weTdwOqrX{bAizx@hsN}stAquf@woHiuKoaAewnAyhTqzgAuvc@goc@uhLboAozIgyHgnUfsNko\\{h\\ywCgdq@fnJunv@uoQifpAzwY{a@nzJ{{HpgR~hYvyN_gOftNqpm@veTrwUteHgzFte^{ndBvtHeeKdk[kaEh|Pmzg@?iwT~`UqcXulFq|_Bzjg@k|g@qyIehx@|cW_nFb`Redr@n}EnkBhyn@se^|oLrrBzmx@gpn@htLix`@luXgzFleMwoNhn[pv@hoDkaIjsr@tp\\dvu@awSfts@|}w@brq@tsJdbMe}Rhzl@}lVacEgzb@`dPcqd@ueBk~c@|sM{sQgVilbBjsKmja@etDixyB`dPqjcCf~Mozc@hkf@{th@pjAkup@n`\\sq]bzM_wo@vaQqoThhPccaAq~CuneAb{V|sM`qZfVlrJ}fi@zdjBwby@fr^sbGly`A_xuAxfc@esFggK{dbDbqj@qp|L_}Ts}hBxs[yuiFun@}xz@z{Mebh@rwb@ko\\xfQaz[pdb@w~iE`|n@euwBawTma}Cxxh@{XrhAktjAlmJi|Niv]ejk@uaiBi`qHaw_@qhrGr|Iy~wBirHwb_@_J_ieB_nRyqb@s{i@eloCwyiBwtmBkoc@gasBck^kjc@_pE_ab@yoPocAsw\\prSeqi@nrx@m`l@ft[iapBeuaA_bVi|i@rrB}wwAzwZsy`@|lJmv^xoWwffDq|_@iqv@gmI{`f@iym@_k_Ac}b@slUicG_c`@rlIe_m@yaCcylAksJen_A_fj@gk`AspEkw_@mkQ}}NunSfoF}viAojdAcvQibo@`wD{beBayh@a_JahEu`a@~aPqpSoYogW{`P_cS{oUe}fAv_HgbwAuiPiuWvcIerGigDi~Mh_MurLpcJ{tu@h|B}|eAehLk`a@qmJygIweb@faAamQhtWyo_@ruLyzp@zspAw}JioDo`Ihla@sdYfzHgnQhv|@a`h@f~j@gbKpgeAdzEjdpAfzaA|hnAsb^tzw@k|Ktjo@rxFv}}@ydDv}p@r}a@ri_Ambk@ldaA{ij@j_KyrTayv@ggw@qAyhm@unk@w`Sc{@_dyDmg}Bxj\\e|gAd__@oewCw_vAyu|C{fEwjvAavSizj@smo@q{FsbIbxi@ajIcvS{_]_wHk|Ygtf@kw_@cdCcq_@_pj@{_[kdCiDaiIwnJa\\a}IcwQukIf~BqtAwfJifr@my[qci@iukAs{Ekd]ebNsjKkxJ|sAqmLs~NykVvbCwjTpwX_de@kbRm|Qfvs@tqHpdhAg|Ez{GqlYdD{pScho@_|oA_`sBhTsh_Akyi@}lxErxGiqOwnC{mxAysFclx@utIoiRtuHcvk@ctGklE~lH{jDyrHmcM~bTsqdAr_Skp\\qmCksXpgXmoXnhFkis@xtR{ti@mcNckDyeDaka@bd`@slvAgzBipR`vNc|Tv{Qd_PmgCahd@vtZkgZa`AkuTxlHtiIdfHipExfDnwOh_Lkq@rlIigj@h{K|iAvVywMxzOarMjzJtyEv~FgcW`wMb}V~nLmvm@z|XhxUvbq@{vg@hjEmdNxxXfoHzgGynXjoUpk@bmDciTt|RzrRjhFy{J{sKawDym@cfMfsU|aLbe[keDxa|@kmdAbyM_In_RkgNpyTjgNb}HwqLf~O`mDviV{wq@hwb@xhSb`m@{cIlmCesSrxSwqJnxMco\\uqB}bUnpKy~k@ycMyn{Aj`HijXblQtbG`xDsnm@baQo{UqeGy_S~tHi`WekOyjFrV{{RlpXa|L`eBac^fiNe~DpfUe}a@`~Iec@h}Zywt@rhAih{A~up@tf[n{IetP~gNwe@sTw{XhnMdaBhiQs~Npob@zj\\|`M?~ab@ojl@hn_@sxE`~Eu|NcxJ{clAvyNqbc@ssMglmCdtDopd@e}`@{~ZapLkh@`yAksXci`@_{v@zeAumsAyk]ouh@amXws}B``Vk|_Aj~Wn~ElrWpcWljy@ssh@bxm@bci@b~H`tn@|aQbgX`rL`wD|jZihMdfH~bS|lPhwIhw|@vrIz}d@d_`@hrNkePt`IpwLjcDygInpIrsAd{Uf_i@`gNckEvfXzaKfxIpa[xpJzfFtbGctEnmL|iLvnCuvGnoJz`AzcY`by@fjg@`fNj`b@jRhrNvta@_gg@n_yDsfSlrUd{V~jXhhBdsQzfT|sBpaXpfa@peItsiAxcLjaV~d^ozXzryBoxe@loN`hJpiuAl~Gx|N{dUrzLkh@vza@|yTpxN_|AdkPbsStuVkyCr`KdrTwiBhpj@dnXvweAlhJhTs@}pR||IahLviIymAboF|nI{tNlvD~~JppHmb^rad@eyd@zrCowBpkZaxJamCswAzen@h{BqiD~wArxI~jk@jmQphJg|Dnxl@lmO{a@rxT|hNj`Je~LvvRgZbjQdzW`q[d_CetEjtIdfHtoC{|DntXvho@dkIpsCdcGfzoD~iXuqApxu@{yp@dmg@ps]}eFlqXyfJztAb_Gf|eA__KpsQy~DnvnAg_BhzDmiH}sMkjJnqVmmP`eBmjLj`U`pSjqZmyFbiT`xE}mHj_Pvl^rbb@xyKbbOeaChUvzNvfe@jm_@p`Nvn\\zr^|}ZnxAoiDftAdfo@ftMttUtcGkkFccB`tT~{\\r}]_hBrlVxhUfks@|hMcdBykCbn[hz`@~vb@vej@zddAveNiRvbNrbLlzPbt]`uCbcfAmwJfkXy~FmtAxkYlwKesAxgNx{L`uO{bRraArmHzbSa_GrbKrhKmrAnw@n}JdiF}tK|lDj~GqCvxh@hmLfxG{e@vpU~bs@dblB|i\\`ea@bcGr`AdpAujJb{OfoVxjBx`]hpEexEsiDtxQjxIejL||DntCepNps[`rD_wAze@|bSbfEihB~pClvKn}DwvStbHyOxqKvwxAvcN~cJ_fHluKqaVokFixBn|KwjFkeEc|Cmgi@qwMi|FrtDy`QwfMkkg@aka@zlOwdAasPwxQqmRemDwz^|wEtmpAwb[npPc|B`ph@m{U_~m@~qC{pLg_JghBksLhcQaaK{rAekb@gvmAqaE|cHoxRu}_@miKfYizFcqReeOmyAmpd@ife@myRjq\\}aRgw@heC|iF{u\\f{s@qsUb|CqhHmeGd{JzrPxwOnmFjn@dc_@kdRrvh@fyFpbK}lCbcWtkWbrWyvDlgLbzO~gGrcCozNbzGjde@toM`gD|oe@xjp@lzEi{Gl{FtmPx~TzxDtd~@t{uDxzMp]dxB`xRd`f@`f[t`D`|M`qEu`CasFkdIvhd@hKnuDlnQhxj@fpq@re@jeEomL_xMb_ClmPi_Gvi@tbKfeNzNnos@lbVhgf@m{c@~ey@ndTv`|@rmX_uJaxAjiMf{b@~_Vddd@x{Fpln@iu`@l~l@aecAbrMai|AobNizT}y@grSthJq`d@xuFr|CpnQq_Mlho@yjr@v|FrdGswKt}[zv[xlRhdy@|u@zxUa_c@lkJyvx@qbE}i_A{`R}vWgsT`oFqr\\ie_AwpVijY_bIxzSsaAez\\geZudgAxzAktc@ngTizSvx@uqSlfWqqEqzG_jj@|~CkaFf_ErkH`zY_zc@{`AgtSwxAliPelDqhHhiBscz@rpFyxHc_S}iF}wEgxQpf[o~JsbGqcKrbGm~Hbs@ookAluK||UvtHqtCkxCxxGlxWmnNvbOx{]{qGvgBjgPiRttGxtIef@s{YviX|jEq~Flv@ngBzbQ~`Ig~DhiHhwUsgDxgYskHggA?inLqiH|cS_jLkT~iL~oCpiHrmT}~NucBmtA~~VxtI_rCb_Tb_|@twPfkActEddXowLgsTvdBdz[xuUbhJrpGzsn@fpOteY{aLxw`@btEbpRpzM~@wmKqpR~`J_cUtiPltAl_OvbOg}OjyJf{EzbQtbGlkDv`Jy}Ehq@xiNbn_@m}JfwFr`N~wM`qt@_cCp_KoiX{qLgcOxwIdeRxgCczHzqc@tkUxm@~uHkkN~iLui@j|BudLmhFmfGfkF_vAhfObcWcxIotAvtGv|Ppx`@~kQyiB`oQvnVzUlrBdtGubIr|Bl_OdxQywGffAteFloOjyf@vyQ`_Ln|Wdgi@~zKvmE~uYnlK~oDzbv@`CkfKkd]lxEctQf_KoxA~l\\yid@njj@q_vBtluAczZ|bYspS`vOdcCzvb@ml\\puZolEzyIyeOjsKb|BrbLs~Mj_TouBvfBksLxff@ow@tmUlpMd_f@uys@faUqjw@nhYuyDvsBdlJd|UwbOd`I}kh@x`Qo~Zrei@kbWtjHbl@v_Bp`Rw~e@`ufBblQxm`@uoB|xYqtg@zr_@_sIrma@_a@r`[`cYztj@ja@joYgeMveZcaT~qNuhQa}@e}N~rN{yIxwk@hcKgdMzsQ|nNaoFmnOjdFkdRb`MtcB~ma@aul@zaGmtj@gkMebk@iiGutAlcHkzu@ff\\ipMiiE|_T~eHqxApfX{soA|bb@oja@jrj@ganAxny@kak@z`T{x@~gJbnpAboq@|kiBzxb@zeOhpLzgOtgAftGarR~lPyz@xwi@ntAbpH~uU|_IoxCtsV|jRpsTakNszWdnDqpTmhVazKcVol`@dlZ}_UjmTh`SfiFu{HvtBemTm{Jno[ygRipS|{IurZe{a@aqeAhyIyga@z|_Acbx@fhMevh@seB{xd@dh\\|~p@zid@fhc@`oFzsn@lgTzC{jLkdK`fQd|Bs|[adk@fp@qjIj~J{xAk`CaqE__HelE{s@boRq|Iw~HuzF{jV`hHkyIxlL|zJnvMmyJp`EnmHziV~hCjaGuwP~~JdnHtlD`x]owYkxAmtA{~Fa|@pfIc~GtHzqd@jdKcEd`MqcWljAxaVtfAkxUhtIrsWnyLjnBppQziDajHliDxtIgi@i|i@bxIth@qRveNh}F`]i_EumTrtEajH`qDx_FtwFwnKbtE`qEsmHv{QnmEvfBicDdpI~mDt{C|bSkxa@|mPbnHhuCbxs@mdYdxg@n_Wet]piHz~FgeCoj]bvF{xQhw\\y}L~}AxtLxkAaxQ`uEvtCxfSodMnbOnyLauKhrGlhBj{Xle^tiK{vNrlOupAjcF}bEesCi`Kx|GqtEo_KotBbuQnnN|pF~DzpSl}Ba|QfpHmQjoo@~c^gwIzceAyiMptDxPzkBbyOsvD~xLsbo@viMsmEpp_@|m[kgH|`P~fSi{FpmPbmNhiPytOv{R~bHgrClmPlkJj_GptCwtGjiVzcE{~BtwQrmHym@kfKbjIeqA`tDvk@dsGmvM}u@ze@zePxjGgKkuDtyDzxN|rAw_Doe_@`cK?ppEkpM?`jHhm@ctE`_Gf}DktEtcYxmLuwExeDpbKfyFcrFz}AxxSz_Bw~Ol}HvfBb`Bjpc@pbKvm@z~F}fShoFd}DdhFk}HdgElbHhlG}hElxE~`a@vbP|_UsqMeh@cxIymXm_[ctQv\\zd]fvYh{GysJjzHriD|hFq~RcmHycD`jGy~EupH~rCxpKjvPd`DcoM~vLrhUusFojRbfV`jHfpI`tIgpIzmHte@c_AdnJbxOenJsmH_uM`_GsmTdc_@wi@mpOb~YplGf`PblBu_O~nVapEs`AkvKykR{jNjmHoxAdtAsmTjxNru]|gAd`Zd}H}uAfyGthLpzd@~sZbFlnCmmDd_C{oGb}YweH~}J{|Gl{V|jJgzRfoV_bIlnCweOayBiuTiwNggNtdGema@h}]btQgmD~oEjyTgsIfhMf`E?loZ~{VanGk{Bw~O~{Qvi@pdDkyIlyQxn@vi@stEltAltM__WjTre@~hF|kOsc@rd@l}JmdSquCv}E|wJq~KwKgfChaQynKawCmtA~wMn~VddL~~JhlYzqQoxLgmEu{DdfRfLwwLs}M`dPa~KzDthKrxKd_DwkF`gZzmMefFpqDfpIe`IbvEphCx}Ei{MwwFi{@`mRacIiiJ~uEfs\\xqKewFyRyqJxkMzdE|yEliQnyLqlSgfHkzHhqPmsAti@vuSxcOi{Fe~Bnf^`hH~yMacJ{rAgxF`sEfvGfi@sgJtbCjkNiT{`AnjRvaU_{K_lFwmK~fHo}JdxQtcNikArmGjgPaxBfwGj_GljDn~KofGavCo{L|h\\gjLyRpdQfdDfv@xqVd_JluK{t\\tlJrjThxHaqE~zKpcJfk@hp@reKjeFkgOhxGfmJz{C{iCyfCl_l@`cOitUagC{nY|qNjhDizA}qMloNi_FpoRdz[qzHvzEpcDxaFpxCg}DlbYtrGlZxsIptUbxPciUywHnj@jbFkyIuHbcFtsFatDpcJjiDmcG|~NltAbhi@heSym_@x_CioGjd]xeMweOhbGpz@baJj{Iif@bt]dp_@r}BoaNlkD|iKj_AzlNpjYctRzaBl{Ft~UecIrtEd|IvtG`r]`a@~tRxfQynLryo@voQivHiOuha@prPc~IoyGxzYniFajH`xNbjLkxMh{Ex[l`SnxCstQrrRxyLxsP_xDjoNnkLunGr{u@~zC}fLvnTltY~gCrrHemLenDumFb}NpuK_tEbxVl~r@gnLx_@yfKdpVy_Jy~GvqO~c`@c_HzoAexD~sZ~oCc`NhgIp|ArqAezP{jDahIlzQprOdhIqqDwmCuvElmL~{Ao`Dtkf@grF~yEywIseMJjva@odKi}OagIfeBl~NzpXjbFyl@ydAllUtySzsNkrK~oGqy@rmN`}OuiS`eXrxL{qIl{D{n@vyViaWlJ}hJsnN{|Cp~GrmTjmp@nrJp_BpOijO|~NppQxiHo{Vl_Kt{Rml^jmV`yS`oaAyaEtzPvtObjMujLeUsjl@zoYtg@rvOg_[xwI`lGvgA{sc@}jEjxUjbGkxUe_CdxQj_GwbOwi@b`AvqIybPclI_vBylo@wzFlxW`}Lbn^pvCnlEuxEt{Pp}McqO~wQpqBhzBv|s@p{AazY|lUwa[f|ExtIumDpbKjpd@ghj@~zY`iIbbJohAbaAoeIzyXzlCbdOnhSwji@~ng@awf@huTxdP~^hd{@gfn@iVdkRteLlbIv~J|VxwDpoPavb@rcMr{OvhDq]xgFnna@c^`eQtgUomTxuOzxUtib@boM|fF~L`dJ}bMdkWe}Ib|A{tKwbOjaAzbSgaUui@|iPx~Fjya@y{GxaQhsUesG~gSqrPc_Aj}G|b_@u}O{pAzvJbaMwvRzkNtmTmtAx~FgxQbaR`zXirPnlEqxAbt]zxIwbOt~NltAnmDi_Gim@|sM|gQfbOadIrnHriHmtAz~BtiVmmPbqGpmLcc@ok@~tMvyAmbGv`P`wY{uJcsEcfEpgTzlBbvEfxAgjLzeDb}Y{eD}bG{bGz~RdxQ`jH}wFseKniAwcDpiH~wMziDx{j@`xY|{U{ePyeDzeDbuEumHvi@qlGcfVybE_\\~lKthW~m\\qDofB|}JfcCmsAjs@j}Sv_DjyHexQti@fxAwsIstEpsEwmDkbGbfDpfIuiPkTymFzkNb|Sy`Ewi@n~Jj`RmsN~~J|u@hb\\rgb@epGlpCzlN`gIf_[mtA?omh@zbRw{FlsB`jImlPpzA|b_@j_Ge_C}iDj_GntAhmLmb_@|mP{xIzx`@~zW~dI|uWh^ty\\unInoPzdKvpV__PufAwrN~zK{yC{}FdpNykNwcO}iDcaMzkNf_KltAs_MrqQkmNibDj_GzhDkgVjqL}cGogT`wCvjMk__@|iD~eHd_CstErwErtEksAekV~`JgwSkzHwmPjbG__Q{bSkmZvm@wi@w|]qmXe~BmtA|bSjmHzhDsbWj_GfxQ?mxE`cWubS{iDbmHhnL}sMym@gcChtImtMaqEb_OfjX`dOmbT`rPleGjtFvwSmlDnfSbnH`yMupIre@liL`vC}bClvKzeFbbAcoH|hRsuJkaSybQvi@?lmPc_Gc~BpsDdbZ{sLteMu~NgmLjpAftI__Kjq@iRjvJfiMofG~vBxnKt~Nu~Nb_GhtUm_[d_Ch_GjcGumDrsEwi@ssE}dAleFgnKw~DdtMvbO}dMloCocNyoKg_@~tM__JkpAbtQr~NsiPntAziDpzB~iXoeHnmP|iDjiD}iDkaGq|Aeg@{oApiH~wAgl@mbHhwUnmQ{pAb{NexQc{NeI~qOsiIfvGvjb@rfJrrGteMidP}cGupI~bHp`LrxDziDbdc@vmDcmHlxEhkB?ukJtpItmTupIe_Cqt^jab@j}Knr{@ywLpuMjhIteNmfVzvMhS|qR{sQ`b`@qyUkg@p`AbkP_zPrld@luFxwMaup@k|@kyOduD{}Ez}PieQ`|Aot]}xHbbBacS{oi@ilTm`Xx~y@hyLlkl@goVraWgAj}X~xLnk[weZf}^eaBdbZ}uQrpCkah@tei@vyTjvTjtPvr_AjfNzaM~o^q_DvtSv~VgnBdw[nf\\pth@e{I|dSoaOcZflEjc]zpv@zzWu_x@t`|@|`EpbJhmc@~zOup]|{RcxHp~Pb{dAvxu@eeHhuWo|VdyLivUd{n@r}Bb}HyeM`hLpgRpzTx_G}pDpri@hn_@`qAxrTuvHd}TxmB|dSx|HfsCixUj`U~fBb{Kzlf@jdPtdEduO~fu@axPrq\\wd[fgRxkCr{PquM~vIveL|wo@xbCr|O}eT`yPlfLoiEv_a@qoHqL_qBt}K~uH|jt@chIz{EusWyuC_eEr`Kixl@dOqdXvxOniThlo@zxc@hsh@zvApf_@}uM~kUnjQt`b@_kUt|M{hSsaJa}Klrd@ij~@`p@mz@`nQzgFrfDaiIn_RhdEtwFafLrh[nW~zv@c|HrdTuxm@ggRo~R_lUswo@jvHgyTkxSqlMnE~YajWmqX~wGobMlb^nbMn{UkeIrp^y|H|qDkuBlu_@yaV_bIoiDzdJ}~NaaF{rWpfTgaNstM{~[vgKooFjhXaa_@h]w}Vwpe@waKmz@ftLbxu@{sIfj\\prDzl_AdlNf_i@lja@byr@wwQhcTy}f@owf@}lXhIoyCzlMorGsxGswOdgGcf@xdYqrv@ayBkxSwrXo]axRu~k@dyK{u`@uxR|zDygTkkSsjYymO{bLe`i@anDnpI{q`@{jt@uic@f{DqzT}tKgtNsxUt`T}aWreFsyFelM`jH_|Cq`Nw|JkpN|jKwjgAqcUehm@l_Gsmc@kpCexWrvKiq^szEq{m@rpDe{Slps@zaXpgDxvBrhPcxaAhe]wuEogF}vJpuLgw_@igAuhMv|WcvIsJ_{OdsQ`QdyXy_^~oQbvDfbJehHxwO~r[xaOptOoNvsN`hWkx]pzn@dvGb_U}uSlro@ebAfj]hoLfnd@eiGtfLgcs@}{e@gjCa}QyjH|`@mfm@rjYeeG|wPlsQhm`@iw_@j`C{hKguZqtOkvHbcBlqXkzWffQfnDlqlAlzVrkDyiDpy\\baFxqHvlZnlD`jE`nVg}Ut{hAonA`oh@o`VdcMsfEvjSjtQfsO~gMnkk@pwHlgBzjMg}MfyP`~QzgPnlq@zr^lpKzbAz`}AtjLneXrn`@~pXrzPqeC|jJrnl@xc\\|zx@hwM`wFj~Le`RrvMnhInfKhfTneDhx[{xE`xEtsLdkPomNvpKzlZvbw@i_@z`PgdNnnFsxG`ve@a_Pa{@uvIlxi@g{QdcXzbX|om@c|W|ajAloBnpd@_m^c~TozKdff@dkFlsQq}QvzKnjGbm{@bsXxh^zuHt}d@vzr@d`k@|}JjuT~td@rqR{E|yJlvOjvOzpi@fg@wRnpNajVl{XorTftBiey@k`U_p\\`aFmtTbng@~`Wfgl@`|E|jr@zzOoNogHr~u@nsRrmTotDdce@jlElv^u_I~_@`wDfsg@spWfi\\otLv|r@mmOjtJzHruWfs\\jlGthGfsOcbQblj@ca]feHnqNd`Pfh\\yrGdcGv{Xslq@pyUsgNndXqeUtbGnlO`zNajIpleBiuXj|@wb^ghOapGvuSjqRhuW}ThwUceKveYw`Hs_@akGh`LmzFph_@omU`oM{|Ptd`@gtOmyCkcEx|I_{O~lGqoHwvDooKp`M}kEzfa@bjKbwi@j}Lj~IesDdqUecu@f|_@ukAjyPonLjRcnNn`PnhGzeTwsOnkPnYveZqmr@hqx@ijYzwjA}`WsJctHzxLmkRsVq}Ofv{AfhP~lSq{GpxVfii@diFzgEhfOpyUhxHqu@bgVgpm@|wVczKv}d@qbZdec@_nRdjpAimUtqA{zH_tKiiLloMhs@`nQqoa@lvk@toC`}p@}sOjla@swz@l{^yfTf{SmnCl}Jbmd@nc\\goFlja@ewYclButK~hKm_RaiVuz^~{[oyd@qkMczr@f~v@opc@dc@ipXbdLiaOcwcAbz_@kwEpoU{q`@swCgwX_zRcjFewJoaa@atJsmEjfJ}_We{d@ira@yon@bjg@_jCbvNujEoyK{sObbDc_ZieO{gF||dAggRbfb@!ch`yBc|adTb|cAspRdf@hmLdxQlmP_nDxtIwtIw`DncCtkIbxUgJzaS|uW~fk@nmP_wAdwR|~NoxM{fK}iDxqn@xmLvqOt~NfpDo~J{iPcaAtxQw`Ezs@|wKziUlxK{`AtiL_hB_xMetAhm@n}E`|Xz~^ztb@qn@dpIrtEcmHzsQ~fGin@`_MgvBwxG}bGwi@niLrcc@etAnmP~~VnxA?n~JhxAimL|`Iv}DokFxcEfxMhtUumP?v{KvbOkvJ}lDkaF`_LbFjdu@giMheDin@zi[or[lwz@e~f@ku@ggXzrLqoQmiEex]j_GitIgiLaeV~_JebSisJihUmmq@soSulKqgIe|[wtMytIltAexQwbOrtEhmLrmTiwN{TicQ{fPgcCm~I`uOi_]ifCkxUifDp_KuxE}gF{sEd}DqqJynd@bkIpDle@onP_xF{r@dxJayBkvJ{|S~qOogHgaIorC_hBqhTuhE{fDxfCr{NwbO{iPfy@mtc@nhYggMex]dxEt~N{qd@`zMu|Dwq[tvFwjQkyTf|\\eqTz~A_q[!itgaE_yncVlmw@ql_CxeK~LdpFnuWsrP|su@{sWveq@scXp~YeyGykGZkdS!_dgvDwasiVzuPprEiwNr_e@xjBnrNkj[l|IcG{jZbhVgxj@!}p~zCw~szU`_G~wMptIaiHbaA|pMj|GnzAieOljPstEsmTiR~cIynL`qE}vKoyLz`SwtT!crt_C{draTxe[~y@eiL~vMxzBfee@etMkiDbr@}dF}`Be}Csc@bjH_eCk_SpxIomDkbG_fT!e}`pCqjkkUneDaa]hhQlrS|mKa`Axt@fuTgpSyhMa|@tkIgeNmhE!itfkDez|bVhfYmnHhsGh|WkmQ~aEglO{oT!an{cCozioTbhDorOdwFjuKrwUkpAnm@j|G}cHt}DuhKeyFsbAbgKeuLozK!ky~cEkfhzUtnf@ibPkmHtmTm_O}iDqmL|cSiQkdR!cwnmFsmasU`zD_cBxcMbsIrhFvgUgbTfJjwFwzNs}LkgM!uz|oF{ibdVduRbDeV~pOmhL}iDdpIbnSopPczNvi@e_O!cdajCofitTjgDf{EkkZdt^gOmoNfsU_`V!c_ecCsujjTzbFmgFzzIted@iiNgaDmtA_{W!chytDmbhhVhuUmoN~tBptCq|MvkXwmJ{pM!kc{tCk}woU~bCmjPltSxwGw`En~KuvQ{kC!cp_xDkwaiV`|LzbFgtHrcXaxIs|YfpE{iD!u}i`C}zcbTr{OxkMgjMfoWw_DgoWpgHghAexEqbK!wwxiCgb`tTvtOyjPzuIke@y_Vfh_@yjCavL!{d_nCulwhU}gI{ub@vgOiQy~Ddhc@!gxocCo`pnTlcMo^|bGflWmnNw}D}wE_nP!el~}C{n{vU|`Iw`[dpBkV_rCbx[a~QbmH|}HcmH!_o`{CyzlwUtcXfgA`}A~lF{uJd_B{jOmuK!kcyuDeqbjVbiVfgAwpVzrLrFc{N!as_qDixdgV~gSku@_z@lbHgtMb_CwwBelJ!{pw~DiyveV~fRotMyqVxob@xiCizS!cvzxC_ytxUtrGzhCgmK|mOmoCw_D~iGawN!ghqvDq`|fVneI_rDr~GxyHaaLt}CacEoeH!}jyeCs``sT`cOmoC{iBdyQexKwhM!{uffCwp|rTj|Nl~HtItvEizGfm@wkFkdQ!is`yDgyiiV|eLe}ZobClj\\mbHgl@!uksnFsxxkVdY|dRkrF`zNdrBubZ~dBi|F!_vm~Dw|jfVbhEybPlrGneHq{Mh|F!gr`nFc{qiVkxDqpEupBqiHvtI?ui@b{N!g}mnF_jokVqn@gcOjc@}pChjGl_Ic_GvtI!aiaiDihsbVr}G``A`ExhMocDecBe_CueL!{jxpDmexfVzvEr|BcxDxqJm|Cik@t}BccM!au_}B_mgaTn`I_[tw@|rBmgLvvFfm@unI!qzdpFinomV{sEu}CrzBiyHtiEruEmpBjaG!eoszDcbgjVz`GqoQeLh`RusFwO!}criDimyaVegEeaC`jBkqLjyBjbFg\\dpI!unt~BcivaTouB_rOjkIrmG{tEjcG!civwDgwbjVf`Dx~Rv[llD{cF{`Hze@kjO!wcn~Ccg~xUnnKebCqcE~}K}iEyzG!emckCqt|sTtqHmwCvbEtt@{`G`zEqsFiwB!umduDyahiV~wLmgF{uJb~McaAuuF!_yueCm~ssTk|IstErwEqrDvcCdhK!cgynCsuaiUjuFzo@mLxzH_y@rF}mDisJ!_lueCi`txTpwCtIhvBruQ{nGi`R!o~omFgnrnV`sDadItmCdyEwaIziB!{w|dCiuerTbgKcbAk~B|jEwgGygC!qpicDmjq}UclHxn@zdFy}FffA~mE!qevuDyqggVzvD}`I|Yj`HyqEp_@!ohsfDeyxaVfrAydJl}Ar`FupDdcC!azj_Cc_aySa|@ngFwqCmhExnEa^!krxgCgpltTa`AcgH`iE|cF_hCdbA!iamfFcry_VmUgyEpaDhPckC|gE!e{vfF{`b_ViSotCnsCfQe_CfbC!izzfFu_v_Vd_DgeBwcClhEmZebB!gigeCwbeuTr|DgoDid@tdBiwCpiA!uhxhFeyo`VyB`sDao@ycDzr@gN!}lhiFqaaaVqb@jmBaQecArt@ei@!qmi_BafweTcUdeAkUeeAnk@?!urveBsoslTxPzr@_m@qCd[in@!okzcBibehT}w@hTv^ei@dXzS!od~gFu_a`VbbAioA}lDd~DxiB{mB!_bvdByfaoTYzq@oPcEhQwk@!ao}`BwkciT|Wzr@iPaAsFyp@!ylhfBsoslT{Rcb@jTrBo@n^!m|ddBupshTqQtGxDiPvKrG");
    private static final double[][][] EXCLUDED_MASK = decodePolylines("ylthCmoexTfdCb{BjbA`Cux@wbC{jBwgAyCimA|fAbGvuAwgBuYyp@tpB~@lg@ggAdY}x@mcAcuEpa@wMptIr{NsoBzkC{CpxBllCq`@k~@`|Ar}@dxErjDusG`hCfNkdFqsD~[atDvbCq`@geBweBmoBhmAimAe|Cct@bD{|AmnCbd@wj@p|@rb@vNsaAxfChQrs@_\\ce@q`@h{A_]anFykAfcCs_AdWswDdqCuHkf@byBtbCdgAjaAa{AxdC_]g\\lrAhjB`D`o@`{AlrAhS{xB``@le@r_AovAnx@c`BuHaa@~uA}t@dJxo@|rBwy@zhCjmA|x@||Dio@{nB`~@bWzp@taGowAzX_tBffAaDuHwhAvy@ko@h~@llC|qAqBqRs`A`p@iRbcAdg@_kA|qBe}C~]ue@lt@uvAuHuf@tbA`eDvN~vA~vAscFpnGnz@~v@n|@q]aRho@tqAvhAqpEhlA{{BvzEx^`DokE~sBvi@xl@iQ|qBj`BdmJ}t@ra@qo@zhE{}BhjA|f@bf@zt@a_Ah}@byDuvAf}DghAkWwaCfdCsoBerG}lC{pAoy@oqCsxDwbCfN_wArqAtHq`@_tByjBqCoy@{iDowDmpEri@g_D{`AshA?_{BekCuqAiTmeAlo@k|@lAmcH!yjnfCiszuTyo@hl@cvD??q}@a}@?uJin@cbAre@c`BwbDu~BeaBhDatDvj@gj@}dBopEhRs{A_iFw|Fvj@{RyjBmpC~y@edAneB~[_\\ntBj`BdJwMvhB|qA?}V_z@fhFjS{R~qC|`BgiAbcAp{AfwA{kClcAp{Ahp@|pBitDbG?tyE|qA`vCv\\zRzs@c~@zb@~x@hEjoA}qB|tAd}Cz~G!c{`gC}`_xTvvAcsFftBu`BhjBte@hnFsbAnMxlAy~@bcA}iD`~@f[rc@vz@sc@|t@dcApvCio@nMvhAmoBeI_NznBk`BgMwj@tcAct@cFjqAnuBimAlWet@jfEklCffBqjBydDny@ieDcmAiaE!{wjfCo_uwTdItvEcsFkS^p|@yzBgNpjC_vBwOgk@bcAfk@dIqsD`mAb`BplAef@!ojgfCeu}sTxAwvF~vBnuA|J{mBv[hRr~@ymAjHt`C|`BnAnMbyB_|E|ZiiA`~@st@uL!mkqfCcv{sTrEcwDpp@yQ|Vxn@aP{p@roAbb@|aBhfDudAn[ulCc}Ag^dcA!cahwCav`gVxqQdaOb{MhgBjmNmsApxIp`@xeCkpAntBwoIte@?vm@j_GvwFs}A`yBzRnhAzlCvkFa}@doHzuJdzEqC|vKxiCloCowA~yLhmL`xBde@v`DdyEdjKveCjjIrrExaKyfC|iEzoBrwPxfBhrJ`vCneZt}DjtElqB~s|@xqJrgPdlIxhTzu@txEnrBziDrtEbhJnqDx}KpwBv~JrqGftIlwKj_G?~qCjoAjeFx_FnjFn}J|aH|~HjpMnkFv~IpkGx|Rt}CrkNjbGfaJtf@~ql@mY|cHr`BpfCnmDncO_\\auCxjBw~DzzIui@d{DtpCrFbs@|u@yQzoAkcAnvA_mE}Wc`G|lCg`JuIstE}sAoob@~wMkkNnaI_nElaIcnBde@{fJ|gRsHliEa_FfuHcxJz~GdqIcnHk~Hb}B{zHxsIycE`xB_~DkmAu}Bbc@_cO|`HgbDzTwbCwj@utGlmD{rLfhAidDxfC_iA}oBwcCa~@uqBlX{RpuCllDb_BaBxmAy~Fte@ue@r_Bb{BxbD}u@bdAchEmY{`AmfFcaA{WmtAh_Gwi@?o@cyDaoEbyDmvAgOiiBszC}zCzr@eqNg|D{r@saBmu@twAwwAwd@eZqiEedAzRjUbzC}fA^ezBqy@uCevEmrKfyDayBwl@q_@fgA_|FwgDtJdjEa}DxCaoB}kBqpR_Kk{J}hDixKctAumIezEa|AwcCy{GygBqpEexEksWcjJe{CwzEa{Bui@ovAcyBkxIq~@qiNq{LstKmtB}|Ig|EsuEebAkcRsvPeeFa`AkzOqjGgj@w_DszCwgBcgDapEgAsnHouBzUg`DaxCqbDs~@{e@mtAikMokFs{Bvi@sxC}pBupCca@}}BkkDg|Ec_AswJkcGwtHgoImaC{tLysDahHmdAssEayBshVkiDcnHnNqpEftC{mBg]mqA}`HpgH}`Ce_CokFc|BehFo_Keu@}dGlZ{_GngAyiBroMobIkTexE~bCzp@rkCmjDbmG_|c@fk@ykAx|Fp_@rqGqwBbEcmHvvBokFvbIpmR!yqttC{zrqU_`@kpDfkCugDbpGanBl|D~W`cBbzAc_CfyGpPf_EriGntIabAduEwk@nZwrCmqAazGtaA}bBewElmCmmFmQgeDkrHaoA!s`aoCwvgyUl{DknBbxCyOa{A|lDlgAh`EfrC~z@vhAx`FeqCleFev@o]vwAu}CwwAeyDe_ClmDti@vi@mmD?tJq{AagC{}Gj{Bvi@{Ui}Ewy@ce@!{qpeCyz}dV~bCu}Cf{@rFbe@v`CsiDtnIcpA`|AwrB_Zs`BbaA?{pMviFhR!kl|nCkzexUkuJq|AcbAgiAznAecBbnBtcB|oHnz@jt@fsHugAgk@cb@e{C!ut_nC{~m{UwoUm_T{Noz@`~HlvEfaG|gGh~CpzF!}weoCw_yxU}gFziD_xAgcC|`DcyDrr@~x@t}BdJiQhmA!efeiCq{kdVktE~uAgOanFlw@wj@txEvh@ok@`yC");

    private CoordinateTransform() {
    }

    public static GeoPoint wgs84ToGcj02(GeoPoint wgs84) {
        if (!usesGcj02(wgs84)) {
            return wgs84;
        }
        return wgs84ToGcj02Inside(wgs84);
    }

    private static GeoPoint wgs84ToGcj02Inside(GeoPoint wgs84) {
        double deltaLatitude = transformLatitude(
                wgs84.longitude - 105.0, wgs84.latitude - 35.0);
        double deltaLongitude = transformLongitude(
                wgs84.longitude - 105.0, wgs84.latitude - 35.0);
        double radians = Math.toRadians(wgs84.latitude);
        double magic = Math.sin(radians);
        magic = 1.0 - ECCENTRICITY * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        deltaLatitude = deltaLatitude * 180.0
                / ((AXIS * (1.0 - ECCENTRICITY)) / (magic * sqrtMagic) * Math.PI);
        deltaLongitude = deltaLongitude * 180.0
                / (AXIS / sqrtMagic * Math.cos(radians) * Math.PI);
        return new GeoPoint(
                wgs84.latitude + deltaLatitude,
                wgs84.longitude + deltaLongitude);
    }

    public static GeoPoint gcj02ToWgs84(GeoPoint gcj02) {
        if (!usesGcj02(gcj02)) {
            return gcj02;
        }
        GeoPoint estimate = gcj02;
        for (int i = 0; i < 8; i++) {
            GeoPoint converted = wgs84ToGcj02Inside(estimate);
            double latitudeError = converted.latitude - gcj02.latitude;
            double longitudeError = converted.longitude - gcj02.longitude;
            estimate = new GeoPoint(
                    estimate.latitude - latitudeError,
                    estimate.longitude - longitudeError);
            if (Math.abs(latitudeError) < 1e-8 && Math.abs(longitudeError) < 1e-8) {
                break;
            }
        }
        return estimate;
    }

    /** Engineering mask for regions where Amap exposes GCJ-02 coordinates. */
    public static boolean usesGcj02(GeoPoint point) {
        if (point == null || !point.isValid() || insideAnyPolygon(point, EXCLUDED_MASK)) {
            return false;
        }
        for (int index = 0; index < CHINA_MASK.length; index++) {
            double[][] polygon = CHINA_MASK[index];
            if (insidePolygon(point, polygon)
                    || (index >= FIRST_SMALL_ISLAND_INDEX && nearPolygon(point, polygon))) {
                return true;
            }
        }
        return false;
    }

    private static boolean insideAnyPolygon(GeoPoint point, double[][][] polygons) {
        for (double[][] polygon : polygons) {
            if (insidePolygon(point, polygon)) {
                return true;
            }
        }
        return false;
    }

    private static boolean nearPolygon(GeoPoint point, double[][] polygon) {
        double toleranceSquared = SMALL_ISLAND_TOLERANCE_DEGREES
                * SMALL_ISLAND_TOLERANCE_DEGREES;
        for (int current = 0, previous = polygon.length - 1;
                current < polygon.length;
                previous = current++) {
            double startLongitude = polygon[previous][0];
            double startLatitude = polygon[previous][1];
            double longitudeDelta = polygon[current][0] - startLongitude;
            double latitudeDelta = polygon[current][1] - startLatitude;
            double segmentLengthSquared = longitudeDelta * longitudeDelta
                    + latitudeDelta * latitudeDelta;
            double projection = segmentLengthSquared == 0.0
                    ? 0.0
                    : ((point.longitude - startLongitude) * longitudeDelta
                    + (point.latitude - startLatitude) * latitudeDelta)
                    / segmentLengthSquared;
            projection = Math.max(0.0, Math.min(1.0, projection));
            double longitudeDistance = point.longitude
                    - (startLongitude + projection * longitudeDelta);
            double latitudeDistance = point.latitude
                    - (startLatitude + projection * latitudeDelta);
            if (longitudeDistance * longitudeDistance
                    + latitudeDistance * latitudeDistance <= toleranceSquared) {
                return true;
            }
        }
        return false;
    }

    private static boolean insidePolygon(GeoPoint point, double[][] polygon) {
        boolean inside = false;
        for (int current = 0, previous = polygon.length - 1;
                current < polygon.length;
                previous = current++) {
            double currentLongitude = polygon[current][0];
            double currentLatitude = polygon[current][1];
            double previousLongitude = polygon[previous][0];
            double previousLatitude = polygon[previous][1];
            boolean crossesLatitude = (currentLatitude > point.latitude)
                    != (previousLatitude > point.latitude);
            if (crossesLatitude) {
                double longitudeAtLatitude = (previousLongitude - currentLongitude)
                        * (point.latitude - currentLatitude)
                        / (previousLatitude - currentLatitude)
                        + currentLongitude;
                if (point.longitude < longitudeAtLatitude) {
                    inside = !inside;
                }
            }
        }
        return inside;
    }

    private static double[][][] decodePolylines(String encoded) {
        String[] parts = encoded.split("!", -1);
        double[][][] polygons = new double[parts.length][][];
        for (int i = 0; i < parts.length; i++) {
            polygons[i] = decodePolyline(parts[i]);
        }
        return polygons;
    }

    private static double[][] decodePolyline(String encoded) {
        double[][] points = new double[encoded.length() / 2][2];
        int latitudeE5 = 0;
        int longitudeE5 = 0;
        int pointCount = 0;
        int index = 0;
        while (index < encoded.length()) {
            int result = 0;
            int shift = 0;
            int value;
            do {
                value = encoded.charAt(index++) - 63;
                result |= (value & 0x1f) << shift;
                shift += 5;
            } while (value >= 0x20);
            latitudeE5 += (result & 1) != 0 ? ~(result >> 1) : result >> 1;

            result = 0;
            shift = 0;
            do {
                value = encoded.charAt(index++) - 63;
                result |= (value & 0x1f) << shift;
                shift += 5;
            } while (value >= 0x20);
            longitudeE5 += (result & 1) != 0 ? ~(result >> 1) : result >> 1;

            points[pointCount][0] = longitudeE5 / 100000.0;
            points[pointCount][1] = latitudeE5 / 100000.0;
            pointCount += 1;
        }
        double[][] exact = new double[pointCount][2];
        System.arraycopy(points, 0, exact, 0, pointCount);
        return exact;
    }

    private static double transformLatitude(double x, double y) {
        double value = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y
                + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        value += (20.0 * Math.sin(6.0 * x * Math.PI)
                + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        value += (20.0 * Math.sin(y * Math.PI)
                + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        value += (160.0 * Math.sin(y / 12.0 * Math.PI)
                + 320.0 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
        return value;
    }

    private static double transformLongitude(double x, double y) {
        double value = 300.0 + x + 2.0 * y + 0.1 * x * x
                + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        value += (20.0 * Math.sin(6.0 * x * Math.PI)
                + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        value += (20.0 * Math.sin(x * Math.PI)
                + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        value += (150.0 * Math.sin(x / 12.0 * Math.PI)
                + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
        return value;
    }
}
