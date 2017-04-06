package comp207p.target;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Christoph Ulshoefer <christophsulshoefer@gmail.com> 06/04/17.
 */
public class CheekyFoldingTest {

    CheekyFolding cf = new CheekyFolding();

    @Test
    public void testMethodOne(){
        assertEquals(2, cf.methodOne());
    }

    @Test
    public void testMethodTwo(){
        assertEquals(18, cf.methodTwo());
    }

    @Test
    public void testMethodThree() {
        assertEquals(4, cf.methodThree());
    }
}
