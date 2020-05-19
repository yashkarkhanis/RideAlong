//import com.google.common.truth.Truth.assertThat;
import com.junjunguo.pocketmaps.group.GroupHandler;

import org.junit.Test;
import static com.google.common.truth.Truth.assertThat;

public class UnitTest {
    @Test
    public void groupValidator() {
        //Verify isGrouped
        assertThat(GroupHandler.getIsGrouped()).isFalse();
    }
}
