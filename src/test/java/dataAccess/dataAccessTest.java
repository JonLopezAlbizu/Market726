package dataAccess;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Collections;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domain.Erreklamazioa;
import domain.Sale;
import domain.Seller;
import exceptions.SaleAlreadyExistException;

@SuppressWarnings("unchecked")
class DataAccessTest {

    private EntityManager db;
    private EntityTransaction tx;
    private DataAccess dataAccess;
    private final Date tomorrow = new Date(System.currentTimeMillis() + 86_400_000L);

    @BeforeEach
    void setUp() {
        db = mock(EntityManager.class);
        tx = mock(EntityTransaction.class);
        when(db.getTransaction()).thenReturn(tx);
        dataAccess = new DataAccess(db);
    }

    // ---------- createSale ----------

    @Test
    void createSale_saleAlreadyExists() {
        Seller seller = mock(Seller.class);
        when(db.find(Seller.class, "a@a.com")).thenReturn(seller);
        when(seller.doesSaleExist("titulo")).thenReturn(true);

        assertThrows(SaleAlreadyExistException.class, () ->
            dataAccess.createSale("titulo", "desc", 1, 10f, tomorrow, "a@a.com", new File("x.png")));
        verify(tx).commit();
    }

    @Test
    void createSale_ok() throws Exception {
        Seller seller = mock(Seller.class);
        Sale sale = mock(Sale.class);
        File file = new File("x.png");
        when(db.find(Seller.class, "a@a.com")).thenReturn(seller);
        when(seller.doesSaleExist("titulo")).thenReturn(false);
        when(seller.addSale("titulo", "desc", 1, 10f, tomorrow, file)).thenReturn(sale);

        Sale result = dataAccess.createSale("titulo", "desc", 1, 10f, tomorrow, "a@a.com", file);

        assertSame(sale, result);
        verify(db).persist(seller);
        verify(tx).commit();
    }

    @Test
    void createSale_sellerNotFound_returnsNull() throws Exception {
        when(db.find(Seller.class, "nadie@a.com")).thenReturn(null); // provoca NullPointerException

        Sale result = dataAccess.createSale("titulo", "desc", 1, 10f, tomorrow, "nadie@a.com", new File("x.png"));

        assertNull(result);
        verify(tx).commit();
    }

    // ---------- isLogged ----------

    @Test
    void isLogged_userExists() {
        TypedQuery<Seller> query = mock(TypedQuery.class);
        Seller seller = mock(Seller.class);
        when(db.createQuery(anyString(), eq(Seller.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(seller));

        assertSame(seller, dataAccess.isLogged("user", "pass"));
    }

    @Test
    void isLogged_userDoesNotExist() {
        TypedQuery<Seller> query = mock(TypedQuery.class);
        when(db.createQuery(anyString(), eq(Seller.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        assertNull(dataAccess.isLogged("user", "mal"));
    }

    // ---------- isRegister ----------

    @Test
    void isRegister_passwordsDoNotMatch() {
        assertNull(dataAccess.isRegister("user", "a@a.com", "1234", "5678"));
    }

    @Test
    void isRegister_emailAlreadyExists() {
        TypedQuery<Seller> query = mock(TypedQuery.class);
        when(db.createQuery(anyString(), eq(Seller.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(mock(Seller.class)));

        assertNull(dataAccess.isRegister("user", "a@a.com", "1234", "1234"));
    }

    @Test
    void isRegister_ok() {
        TypedQuery<Seller> query = mock(TypedQuery.class);
        when(db.createQuery(anyString(), eq(Seller.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        Seller s = dataAccess.isRegister("user", "a@a.com", "1234", "1234");

        assertNotNull(s);
        verify(db).persist(s);
        verify(tx).commit();
    }

    // ---------- addErreklamazioa ----------

    @Test
    void addErreklamazioa_saleNotFound() {
        when(db.find(Sale.class, 1)).thenReturn(null);

        assertFalse(dataAccess.addErreklamazioa(1, "motivo", "b@b.com"));
        verify(tx).rollback();
    }

    @Test
    void addErreklamazioa_alreadyClaimed() {
        Sale sale = mock(Sale.class);
        when(db.find(Sale.class, 1)).thenReturn(sale);
        when(sale.getErreklamazioa()).thenReturn(mock(Erreklamazioa.class));

        assertFalse(dataAccess.addErreklamazioa(1, "motivo", "b@b.com"));
        verify(tx).rollback();
    }

    @Test
    void addErreklamazioa_ok() {
        Sale sale = mock(Sale.class);
        when(db.find(Sale.class, 1)).thenReturn(sale);
        when(sale.getErreklamazioa()).thenReturn(null);

        assertTrue(dataAccess.addErreklamazioa(1, "motivo", "b@b.com"));
        verify(tx).commit();
    }
}
