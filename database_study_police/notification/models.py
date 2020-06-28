from django.db import models
from classes.models import Classroom
from users.models import CustomUser



class GeneralNotification(models.Model):
    id = models.AutoField(primary_key = True)

    classroom = models.ForeignKey(Classroom, on_delete = models.CASCADE)
    user = models.ForeignKey(CustomUser, on_delete = models.CASCADE)
    time = models.BigIntegerField()
    seen = models.IntegerField()
    noti_type = models.IntegerField()

    def __str__(self):
        return str(self.id) + " " + str(self.user) + " " + str(self.time)

    class Meta:
        ordering = ['seen', 'time', 'user']
    