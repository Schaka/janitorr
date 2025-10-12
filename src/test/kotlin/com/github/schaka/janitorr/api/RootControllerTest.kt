package com.github.schaka.janitorr.api

package com.github.schaka.janitorr.api

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import kotlin.test.assertEquals

class RootControllerTest {

    @Test
    fun `root path should forward to index html`() {
        val controller = RootController()
        val mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
            .andExpect(forwardedUrl("/index.html"))
    }

    @Test
    fun `controller index method returns correct forward path`() {
        val controller = RootController()
        val result = controller.index()
        assertEquals("forward:/index.html", result)
    }
}
