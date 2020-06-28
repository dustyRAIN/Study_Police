from django.db import models

from users.models import CustomUser
from .utils import randomStringDigits

# Create your models here.
#classroom is extending models.Model

class Classroom(models.Model):
    id = models.AutoField(primary_key = True)
    name = models.CharField(max_length = 150)
    access_code = models.CharField(max_length = 12, unique = True)
    date_created = models.DateField(auto_now_add=True)
    creator = models.ForeignKey(CustomUser, on_delete=models.CASCADE)


    def save(self, *args, **kwargs):
        self.access_code = randomStringDigits(6)

        while Classroom.objects.filter(access_code = self.access_code).exists():
            self.access_code = randomStringDigits(6)

        super(Classroom, self).save(*args, **kwargs)


    def __str__(self):
        return self.name
    

    def get_id(self):
        return self.id

    class Meta:
        ordering = ['date_created']



class ClassHasStudent(models.Model):
    id  = models.AutoField(primary_key = True)
    classroom = models.ForeignKey(Classroom, related_name='joined_class_details', on_delete=models.CASCADE)
    student = models.ForeignKey(CustomUser, related_name='creator_name', on_delete=models.CASCADE)
    date_joined = models.DateField(auto_now_add = True)

    def getStuId(self):
        return str(self.student)

    def getStudentID(self):
        return str(self.student.getID())
    
    def getClassID(self):
        return str(self.classroom.get_id())


    def __str__(self):
        return str(self.classroom) + " " + str(self.student)


    class Meta:
        ordering = ['classroom', 'date_joined', 'student']





