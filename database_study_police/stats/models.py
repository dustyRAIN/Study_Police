from django.db import models
from users.models import CustomUser
from classes.models import Classroom
from materials.models import Materials
import os


class ReadingTime(models.Model):
    id = models.AutoField(primary_key = True)

    classroom = models.ForeignKey(Classroom, on_delete = models.CASCADE)
    student = models.ForeignKey(CustomUser, on_delete = models.CASCADE)
    material = models.ForeignKey(Materials, on_delete = models.CASCADE)
    duration = models.IntegerField()
    updating_time = models.DateTimeField(auto_now_add = True)

    def getMatId(self):
        return self.material.getMatId()

    def __str__(self):
        return str(self.classroom) + str(self.material) + str(self.student)

    class Meta:
        ordering = ['classroom', 'material', 'student', 'updating_time', 'duration']


# content_type 1 --> material
# content_type 2 --> joined class
# content_type 3 --> created class
class ContentFrequency(models.Model):
    id = models.AutoField(primary_key = True)

    content_id = models.IntegerField()
    content_type = models.IntegerField()
    user = models.ForeignKey(CustomUser, on_delete = models.CASCADE)
    frequency = models.IntegerField()
    name = models.CharField(max_length = 400)

    def __str__(self):
        return str(self.user) + " " + str(self.content_type) + " " + str(self.name) + " " + str(self.frequency)

    class Meta:
        ordering = ['content_type', 'frequency']
