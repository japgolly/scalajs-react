import http.server

class MyHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_my_headers()
        http.server.SimpleHTTPRequestHandler.end_headers(self)

    def send_my_headers(self):
        self.send_header("Cache-Control", "no-cache, no-store, must-revalidate")
        self.send_header("Pragma", "no-cache")
        self.send_header("Expires", "0")
        # self.send_header("Access-Control-Allow-Origin", "*")
        # self.send_header("Access-Control-Allow-Methods", "POST")
        # self.send_header("Access-Control-Allow-Credentials", "false")
        # self.send_header("Access-Control-Allow-Headers", "Content-Type")


if __name__ == '__main__':
    http.server.test(HandlerClass=MyHTTPRequestHandler)

