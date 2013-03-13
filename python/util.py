

class display_list(list):
    def show(self):
        for i in self:
            i.show()

    def hide(self):
        for i in self:
            i.hide()
