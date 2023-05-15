package ch.fhnw.apm.keyvalstore;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) throws UnknownHostException {
        model.addAttribute("hostname", InetAddress.getLocalHost().getHostName());
        return "index";
    }
}
