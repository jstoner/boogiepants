

class marks_control(object):
    def __init__(self):
        import marks
        self.stored_mark_sets=marks.marks
        self.active_mark_sets={}

    def activate_mark_set(self, set_name):
        self.active_mark_sets[set_name]=self.stored_mark_sets[set_name]

    def deactivate_mark_set(self, set_name):
        del self.active_mark_sets[set_name]

    def get_mark_set(self, set_name):
        return self.stored_mark_sets[set_name]

    def mark_set_keys(self):
        return self.stored_mark_sets.keys()

    def cross_marks(self, prevstep, now):
        for i in self.active_mark_sets.values():
            for j in i:
                j.cross(prevstep, now)
