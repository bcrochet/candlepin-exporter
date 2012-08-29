package org.candlepin.sync;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.candlepin.auth.Principal;
import org.candlepin.config.CandlepinCommonTestConfig;
import org.candlepin.config.ConfigProperties;
import org.candlepin.guice.PrincipalProvider;
import org.candlepin.model.Consumer;
import org.candlepin.model.ConsumerTypeCurator;
import org.candlepin.model.Entitlement;
import org.candlepin.model.EntitlementCurator;
import org.candlepin.model.Pool;
import org.candlepin.model.Product;
import org.candlepin.model.ProductCertificate;
import org.candlepin.model.ProvidedProduct;
import org.candlepin.model.Rules;
import org.candlepin.model.RulesCurator;
import org.candlepin.pki.PKIUtility;
import org.candlepin.policy.js.export.JsExportRules;
import org.candlepin.service.EntitlementCertServiceAdapter;
import org.candlepin.service.ProductServiceAdapter;

import com.google.inject.Inject;

public class Export {
    private ConsumerTypeCurator ctc;
    private MetaExporter me;
    private ConsumerExporter ce;
    private ConsumerTypeExporter cte;
    private RulesCurator rc;
    private RulesExporter re;
    private EntitlementCertExporter ece;
    private EntitlementCertServiceAdapter ecsa;
    private ProductExporter pe;
    private ProductServiceAdapter psa;
    private ProductCertExporter pce;
    private EntitlementCurator ec;
    private EntitlementExporter ee;
    private PKIUtility pki;
    private CandlepinCommonTestConfig config;
    private JsExportRules exportRules;
    private PrincipalProvider pprov;
    
    public static void main(String[] args) {
        try {
            new Export().export();
        } catch (ExportCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Inject
    public Export() {
        ctc = mock(ConsumerTypeCurator.class);
        me = new MetaExporter();
        ce = new ConsumerExporter();
        cte = new ConsumerTypeExporter();
        rc = mock(RulesCurator.class);
        re = new RulesExporter(rc);
        ece = new EntitlementCertExporter();
        ecsa = mock(EntitlementCertServiceAdapter.class);
        pe = new ProductExporter();
        psa = mock(ProductServiceAdapter.class);
        pce = new ProductCertExporter();
        ec = mock(EntitlementCurator.class);
        ee = new EntitlementExporter();
        pki = mock(PKIUtility.class);
        config = new CandlepinCommonTestConfig();
        exportRules = mock(JsExportRules.class);
        pprov = mock(PrincipalProvider.class);

        when(exportRules.canExport(any(Entitlement.class))).thenReturn(Boolean.TRUE);
    }
    
    public void export() throws ExportCreationException {
        config.setProperty(ConfigProperties.SYNC_WORK_DIR, "/tmp/");
        Consumer consumer = mock(Consumer.class);
        Entitlement ent = mock(Entitlement.class);
        ProvidedProduct pp = mock(ProvidedProduct.class);
        Pool pool = mock(Pool.class);
        Rules mrules = mock(Rules.class);
        Principal principal = mock(Principal.class);

        Set<ProvidedProduct> ppset = new HashSet<ProvidedProduct>();
        ppset.add(pp);

        Set<Entitlement> entitlements = new HashSet<Entitlement>();
        entitlements.add(ent);

        Product prod = new Product("12345", "RHEL Product");
        prod.setMultiplier(1L);
        prod.setCreated(new Date());
        prod.setUpdated(new Date());
        prod.setHref("http://localhost");
        prod.setAttributes(Collections.EMPTY_SET);

        Product prod1 = new Product("MKT-prod", "RHEL Product");
        prod1.setMultiplier(1L);
        prod1.setCreated(new Date());
        prod1.setUpdated(new Date());
        prod1.setHref("http://localhost");
        prod1.setAttributes(Collections.EMPTY_SET);

        ProductCertificate pcert = new ProductCertificate();
        pcert.setKey("euh0876puhapodifbvj094");
        pcert.setCert("hpj-08ha-w4gpoknpon*)&^%#");
        pcert.setCreated(new Date());
        pcert.setUpdated(new Date());

        when(pp.getProductId()).thenReturn("12345");
        when(pool.getProvidedProducts()).thenReturn(ppset);
        when(pool.getProductId()).thenReturn("MKT-prod");
        when(ent.getPool()).thenReturn(pool);
        when(mrules.getRules()).thenReturn("foobar");
        when(pki.getSHA256WithRSAHash(any(InputStream.class))).thenReturn(
            "signature".getBytes());
        when(rc.getRules()).thenReturn(mrules);
        when(consumer.getEntitlements()).thenReturn(entitlements);
        when(psa.getProductById("12345")).thenReturn(prod);
        when(psa.getProductById("MKT-prod")).thenReturn(prod1);
        when(psa.getProductCertificate(any(Product.class))).thenReturn(pcert);
        when(pprov.get()).thenReturn(principal);
        when(principal.getUsername()).thenReturn("testUser");

        // FINALLY test this badboy
        Exporter e = new Exporter(ctc, me, ce, cte, re, ece, ecsa, pe, psa,
            pce, ec, ee, pki, config, exportRules, pprov);

        File export = e.getFullExport(consumer);
        System.out.println(export.getAbsolutePath());
    }
}
