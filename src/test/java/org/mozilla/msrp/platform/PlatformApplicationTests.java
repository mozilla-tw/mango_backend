package org.mozilla.msrp.platform;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PlatformApplication.class)
public class PlatformApplicationTests {

    @Test
    public void contextLoads() {
        assertTrue(true);
    }

}
